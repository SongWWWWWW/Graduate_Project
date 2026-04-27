package io.github.xxyopen.novel.controller.author;

import io.github.xxyopen.novel.core.ai.NovelChatMemory;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dao.entity.AiChatSession;
import io.github.xxyopen.novel.dto.req.AiCharacterReqDto;
import io.github.xxyopen.novel.dto.req.AiChatReqDto;
import io.github.xxyopen.novel.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 作家后台-AI模块API控制器
 * 支持 SSE 流式输出、多轮对话记忆、角色设定管理
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Tag(name = "AuthorAiController", description = "作家后台-AI模块")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_AUTHOR_AI_URL_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class AuthorAiController {

    private final AiService aiService;
    private final NovelChatMemory novelChatMemory;

    /**
     * AI 流式对话/续写/润色
     * 使用 SSE (Server-Sent Events) 实现打字机效果
     */
    @Operation(summary = "AI流式对话接口", description = "支持续写、润色、通用对话，返回 SSE 流")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody AiChatReqDto dto) {
        return aiService.streamChat(dto);
    }

    /**
     * AI 生成小说大纲（非流式）
     */
    @Operation(summary = "AI生成小说大纲")
    @PostMapping("/outline")
    public RestResp<String> generateOutline(@RequestParam String theme) {
        return aiService.generateOutline(theme);
    }

    // ==================== 角色设定管理 ====================

    /**
     * 保存或更新 AI 角色设定
     */
    @Operation(summary = "保存/更新角色设定")
    @PostMapping("/character")
    public RestResp<Void> saveOrUpdateCharacter(@Valid @RequestBody AiCharacterReqDto dto) {
        return aiService.saveOrUpdateCharacter(dto);
    }

    /**
     * 查询角色设定列表
     */
    @Operation(summary = "查询角色设定列表")
    @GetMapping("/characters")
    public RestResp<List<AiCharacter>> listCharacters(
        @RequestParam(required = false) Long bookId) {
        return aiService.listCharacters(bookId);
    }

    /**
     * 删除角色设定
     */
    @Operation(summary = "删除角色设定")
    @DeleteMapping("/character/{characterId}")
    public RestResp<Void> deleteCharacter(@PathVariable Long characterId) {
        return aiService.deleteCharacter(characterId);
    }

    // ==================== 会话管理 ====================

    /**
     * 查询 AI 会话列表
     */
    @Operation(summary = "查询AI会话列表")
    @GetMapping("/sessions")
    public RestResp<List<AiChatSession>> listSessions(
        @RequestParam(required = false) Integer sessionType) {
        return aiService.listSessions(sessionType);
    }

    /**
     * 删除 AI 会话及历史消息
     */
    @Operation(summary = "删除AI会话")
    @DeleteMapping("/session/{sessionId}")
    public RestResp<Void> deleteSession(@PathVariable String sessionId) {
        return aiService.deleteSession(sessionId);
    }
}
