package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.core.ai.AiPromptTemplate;
import io.github.xxyopen.novel.core.ai.NovelChatMemory;
import io.github.xxyopen.novel.core.audit.AiAuditResult;
import io.github.xxyopen.novel.core.auth.UserHolder;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.config.AiProperties;
import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dao.entity.AiChatSession;
import io.github.xxyopen.novel.dao.entity.BookInfo;
import io.github.xxyopen.novel.dao.mapper.AiCharacterMapper;
import io.github.xxyopen.novel.dao.mapper.AiChatMessageMapper;
import io.github.xxyopen.novel.dao.mapper.AiChatSessionMapper;
import io.github.xxyopen.novel.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.dto.req.AiCharacterReqDto;
import io.github.xxyopen.novel.dto.req.AiChatReqDto;
import io.github.xxyopen.novel.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 服务实现类
 * 支持多 Provider（SiliconFlow / Kimi 等），使用 WebClient 直接调用 OpenAI 兼容 API
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final NovelChatMemory novelChatMemory;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiCharacterMapper aiCharacterMapper;
    private final BookInfoMapper bookInfoMapper;
    private final AiProperties aiProperties;
    private WebClient aiWebClient;
    private final ObjectMapper cleanObjectMapper;

    private final Environment environment;

    public AiServiceImpl(ChatClient chatClient, NovelChatMemory novelChatMemory,
                         AiChatSessionMapper aiChatSessionMapper, AiChatMessageMapper aiChatMessageMapper,
                         AiCharacterMapper aiCharacterMapper, BookInfoMapper bookInfoMapper,
                         AiProperties aiProperties, Environment environment) {
        this.chatClient = chatClient;
        this.novelChatMemory = novelChatMemory;
        this.aiChatSessionMapper = aiChatSessionMapper;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.aiCharacterMapper = aiCharacterMapper;
        this.bookInfoMapper = bookInfoMapper;
        this.aiProperties = aiProperties;
        this.environment = environment;
        // 使用独立的 ObjectMapper，不受全局 write-numbers-as-strings 配置影响
        this.cleanObjectMapper = new ObjectMapper();
        this.aiWebClient = null; // 延迟初始化
    }

    private WebClient getWebClient() {
        if (this.aiWebClient == null) {
            String baseUrl = resolveBaseUrl();
            String apiKey = resolveApiKey();
            this.aiWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        }
        return this.aiWebClient;
    }

    private String resolveBaseUrl() {
        if (aiProperties.getBaseUrl() != null && !aiProperties.getBaseUrl().isBlank()) {
            return aiProperties.getBaseUrl();
        }
        return switch (aiProperties.getProvider()) {
            case "kimi" -> "https://api.moonshot.cn/v1";
            case "deepseek" -> "https://api.deepseek.com/v1";
            case "siliconflow" -> "https://api.siliconflow.cn/v1";
            case "openai" -> "https://api.openai.com/v1";
            default -> "https://api.deepseek.com/v1";
        };
    }

    private String resolveApiKey() {
        if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            return aiProperties.getApiKey();
        }
        // fallback: 从 Spring AI OpenAI 配置读取
        String springAiKey = environment.getProperty("spring.ai.openai.api-key");
        if (springAiKey != null && !springAiKey.isBlank() && !springAiKey.toLowerCase().contains("placeholder")) {
            return springAiKey;
        }
        // fallback: 环境变量
        String envKey = System.getenv("KIMI_API_KEY");
        if (envKey != null && !envKey.isBlank()) return envKey;
        envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) return envKey;
        // 返回空字符串，由调用方处理
        return "";
    }

    private boolean isApiKeyConfigured() {
        String key = resolveApiKey();
        return key != null && !key.isBlank();
    }

    private String resolveModel() {
        if (aiProperties.getModel() != null && !aiProperties.getModel().isBlank()) {
            return aiProperties.getModel();
        }
        return switch (aiProperties.getProvider()) {
            case "kimi" -> "moonshot-v1-8k";
            case "deepseek" -> "deepseek-v4-pro";
            case "siliconflow" -> "deepseek-ai/DeepSeek-V4-Pro";
            case "openai" -> "gpt-4o-mini";
            default -> "deepseek-v4";
        };
    }

    @Override
    public Flux<String> streamChat(AiChatReqDto dto) {
        Long userId = UserHolder.getUserId();
        String sessionId = dto.getSessionId();
        boolean isNewSession = false;

        // 1. 会话管理
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
            isNewSession = true;
        } else {
            AiChatSession session = aiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<AiChatSession>()
                    .eq(AiChatSession::getSessionId, sessionId)
            );
            if (session == null || !Objects.equals(session.getUserId(), userId)) {
                sessionId = UUID.randomUUID().toString().replace("-", "");
                isNewSession = true;
            }
        }

        // 2. 新建/更新会话记录
        if (isNewSession) {
            AiChatSession session = new AiChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setBookId(dto.getBookId());
            session.setSessionType(dto.getSessionType() != null ? dto.getSessionType() : 0);
            session.setTitle(truncateTitle(dto.getPrompt()));
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionMapper.insert(session);
        } else {
            AiChatSession session = new AiChatSession();
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionMapper.update(session,
                new LambdaQueryWrapper<AiChatSession>()
                    .eq(AiChatSession::getSessionId, sessionId));
        }

        // 3. 保存用户消息到数据库
        novelChatMemory.saveUserMessage(sessionId, dto.getPrompt());

        // 4. 查询历史记忆
        List<Message> history = novelChatMemory.getHistory(sessionId);

        // 5. 查询角色设定和小说信息
        AiCharacter character = null;
        if (dto.getCharacterId() != null) {
            character = aiCharacterMapper.selectById(dto.getCharacterId());
        }
        BookInfo bookInfo = null;
        if (dto.getBookId() != null) {
            bookInfo = bookInfoMapper.selectById(dto.getBookId());
        }

        // 6. 构建 System Prompt
        String systemPrompt = buildSystemPromptByType(dto, character, bookInfo);

        // 7. 构建 OpenAI 兼容的消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        for (Message msg : history) {
            String role = switch (msg.getMessageType().getValue()) {
                case "system" -> "system";
                case "assistant" -> "assistant";
                default -> "user";
            };
            messages.add(Map.of("role", role, "content", msg.getText()));
        }
        // 最后添加当前用户消息（如果 history 中没有的话）
        boolean hasCurrentPrompt = history.stream()
            .anyMatch(m -> m instanceof UserMessage && dto.getPrompt().equals(m.getText()));
        if (!hasCurrentPrompt) {
            messages.add(Map.of("role", "user", "content", dto.getPrompt()));
        }

        // 8. 构建请求体
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", resolveModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("temperature", aiProperties.getTemperature());
        if (aiProperties.getMaxTokens() != null) {
            requestBody.put("max_tokens", aiProperties.getMaxTokens());
        }

        String finalSessionId = sessionId;
        StringBuilder fullResponse = new StringBuilder();

        if (!isApiKeyConfigured()) {
            return Flux.just("【错误】AI API Key 未配置。请在 application.yml 中配置 novel.ai.api-key，或通过环境变量 KIMI_API_KEY 设置。", "[DONE]");
        }

        log.info("AI 请求: model={}, messages={}", resolveModel(), messages);
        return getWebClient().post()
            .uri("/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String.class)
            .timeout(Duration.ofMinutes(5))
            .doOnNext(line -> log.debug("AI SSE 原始行: [{}]", line))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(line -> {
                // Spring 的 bodyToFlux(String.class) 对于 SSE 会自动提取 data: 后的内容
                // 但有些服务器直接返回 data: {...} 行，需要兼容处理
                String data = line;
                if (line.startsWith("data:")) {
                    data = line.substring(5).trim();
                }
                log.debug("AI SSE 数据: [{}]", data);
                if ("[DONE]".equals(data)) {
                    return Flux.empty();
                }
                try {
                    JsonNode root = cleanObjectMapper.readTree(data);
                    JsonNode choices = root.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).get("delta");
                        if (delta != null && delta.has("content")) {
                            String content = delta.get("content").asText();
                            log.debug("AI SSE 内容: [{}]", content);
                            if (content != null && !content.isEmpty()) {
                                fullResponse.append(content);
                                return Flux.just(content);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析 SSE 数据失败: {}", data, e);
                }
                return Flux.empty();
            })
            .doOnComplete(() -> {
                // 保存 AI 回复到数据库
                String aiText = fullResponse.toString();
                if (!aiText.isBlank()) {
                    novelChatMemory.saveAssistantMessage(finalSessionId, aiText);
                }
            })
            .concatWithValues("[DONE]");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> saveOrUpdateCharacter(AiCharacterReqDto dto) {
        Long userId = UserHolder.getUserId();
        AiCharacter entity = new AiCharacter();
        entity.setUserId(userId);
        entity.setBookId(dto.getBookId());
        entity.setName(dto.getName());
        entity.setPersona(dto.getPersona());
        entity.setStyle(dto.getStyle());
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        entity.setUpdateTime(LocalDateTime.now());

        if (dto.getId() != null) {
            AiCharacter exist = aiCharacterMapper.selectById(dto.getId());
            if (exist == null || !Objects.equals(exist.getUserId(), userId)) {
                return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
            }
            entity.setId(dto.getId());
            aiCharacterMapper.updateById(entity);
        } else {
            entity.setCreateTime(LocalDateTime.now());
            aiCharacterMapper.insert(entity);
        }

        if (entity.getIsDefault() == 1 && entity.getBookId() != null) {
            aiCharacterMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiCharacter>()
                    .eq(AiCharacter::getUserId, userId)
                    .eq(AiCharacter::getBookId, entity.getBookId())
                    .ne(AiCharacter::getId, entity.getId())
                    .set(AiCharacter::getIsDefault, 0));
        }

        return RestResp.ok();
    }

    @Override
    public RestResp<List<AiCharacter>> listCharacters(Long bookId) {
        Long userId = UserHolder.getUserId();
        LambdaQueryWrapper<AiCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiCharacter::getUserId, userId);
        if (bookId != null) {
            wrapper.and(w -> w.eq(AiCharacter::getBookId, bookId).or().isNull(AiCharacter::getBookId));
        }
        wrapper.orderByDesc(AiCharacter::getIsDefault).orderByDesc(AiCharacter::getCreateTime);
        return RestResp.ok(aiCharacterMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteCharacter(Long characterId) {
        Long userId = UserHolder.getUserId();
        AiCharacter character = aiCharacterMapper.selectById(characterId);
        if (character == null || !Objects.equals(character.getUserId(), userId)) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        aiCharacterMapper.deleteById(characterId);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AiChatSession>> listSessions(Integer sessionType) {
        Long userId = UserHolder.getUserId();
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId);
        if (sessionType != null) {
            wrapper.eq(AiChatSession::getSessionType, sessionType);
        }
        wrapper.orderByDesc(AiChatSession::getUpdateTime);
        return RestResp.ok(aiChatSessionMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteSession(String sessionId) {
        Long userId = UserHolder.getUserId();
        AiChatSession session = aiChatSessionMapper.selectOne(
            new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getSessionId, sessionId));
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        aiChatSessionMapper.deleteById(session.getId());
        novelChatMemory.clear(sessionId);
        return RestResp.ok();
    }

    @Override
    public RestResp<String> generateOutline(String theme) {
        String prompt = AiPromptTemplate.buildOutlineSystemPrompt();
        String result = chatClient.prompt()
            .system(prompt)
            .user("请根据以下主题生成小说大纲：" + theme)
            .call()
            .content();
        return RestResp.ok(result);
    }

    @Override
    public AiAuditResult auditContent(String text, String bizType) {
        if (!Boolean.TRUE.equals(aiProperties.getAuditEnabled()) || !isApiKeyConfigured()) {
            // 未启用或 API Key 未配置，返回 REVIEW 降级处理
            AiAuditResult fallback = new AiAuditResult();
            fallback.setResult("REVIEW");
            fallback.setReason("LLM 审核未启用或 API Key 未配置");
            fallback.setCategories(List.of());
            return fallback;
        }

        String prompt = buildAuditPrompt(text, bizType);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", prompt));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", resolveModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.1); // 低温度保证结果稳定
        requestBody.put("max_tokens", 512);
        requestBody.put("response_format", Map.of("type", "json_object"));

        try {
            String response = getWebClient().post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.isBlank()) {
                log.warn("LLM 审核返回空响应");
                return reviewResult("LLM 返回空响应");
            }

            JsonNode root = cleanObjectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                log.warn("LLM 审核返回格式异常: {}", response);
                return reviewResult("LLM 返回格式异常");
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                return reviewResult("LLM 返回内容为空");
            }

            AiAuditResult result = cleanObjectMapper.readValue(content, AiAuditResult.class);
            // 校验 result 字段合法性
            if (!"PASS".equals(result.getResult()) && !"BLOCK".equals(result.getResult()) && !"REVIEW".equals(result.getResult())) {
                result.setResult("REVIEW");
            }
            if (result.getCategories() == null) {
                result.setCategories(List.of());
            }
            log.info("LLM 审核完成: bizType={}, result={}, reason={}", bizType, result.getResult(), result.getReason());
            return result;

        } catch (Exception e) {
            log.error("LLM 审核调用失败", e);
            return reviewResult("调用异常: " + e.getMessage());
        }
    }

    private String buildAuditPrompt(String text, String bizType) {
        String typeDesc = switch (bizType) {
            case "BOOK_CHAPTER" -> "小说章节内容";
            case "COMMENT" -> "用户评论";
            case "BOOK_INFO" -> "小说信息（书名+简介）";
            case "AI_OUTPUT" -> "AI 生成内容";
            default -> "文本内容";
        };
        return String.format(
            "你是一名专业的内容审核专家。请对以下%s进行审核，判断是否存在违规内容。\n\n" +
            "审核标准：\n" +
            "1. 色情低俗：露骨的性描写、色情暗示、卖淫嫖娼等\n" +
            "2. 暴力血腥：极端暴力、虐待、恐怖主义、自残等\n" +
            "3. 政治敏感：煽动颠覆国家政权、民族仇恨、邪教等\n" +
            "4. 违法犯罪：毒品、赌博、诈骗、洗钱等\n" +
            "5. 人身攻击：恶意侮辱、诽谤、人肉搜索、网络暴力等\n" +
            "6. 广告垃圾：垃圾广告、引流、传销等\n\n" +
            "请严格按照以下 JSON 格式返回，不要包含任何其他内容：\n" +
            "{\\n" +
            "  \"result\": \"PASS\" | \"BLOCK\" | \"REVIEW\",\\n" +
            "  \"reason\": \"审核结论的简要说明（50字以内）\",\\n" +
            "  \"categories\": [\"违规分类1\", \"违规分类2\"]\\n" +
            "}\n\n" +
            "result 说明：\n" +
            "- PASS：内容完全正常，无任何违规\n" +
            "- BLOCK：内容明确违规，必须阻断\n" +
            "- REVIEW：内容边界模糊，建议人工复审\n\n" +
            "待审核文本：\n\"\"\"\n%s\n\"\"\"",
            typeDesc, text);
    }

    private AiAuditResult reviewResult(String reason) {
        AiAuditResult r = new AiAuditResult();
        r.setResult("REVIEW");
        r.setReason(reason);
        r.setCategories(List.of());
        return r;
    }

    private String buildSystemPromptByType(AiChatReqDto dto, AiCharacter character, BookInfo bookInfo) {
        Integer type = dto.getSessionType() != null ? dto.getSessionType() : 0;
        return switch (type) {
            case 1 -> AiPromptTemplate.buildContinueSystemPrompt(character, bookInfo, dto.getContext(), dto.getTargetLength());
            case 3 -> AiPromptTemplate.buildPolishSystemPrompt(character);
            default -> AiPromptTemplate.buildChatSystemPrompt(character, bookInfo);
        };
    }

    private String truncateTitle(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "新会话";
        }
        String trimmed = prompt.trim().replaceAll("\\s+", " ");
        return trimmed.length() > 20 ? trimmed.substring(0, 20) + "..." : trimmed;
    }
}
