package io.github.xxyopen.novel.core.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.xxyopen.novel.dao.entity.AiChatMessage;
import io.github.xxyopen.novel.dao.mapper.AiChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库持久化的 ChatMemory 实现
 * 支持多轮对话记忆的存储与读取
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelChatMemory {

    private final AiChatMessageMapper aiChatMessageMapper;

    /**
     * 单会话最大消息数（防止上下文过长）
     */
    private static final int MAX_MESSAGES_PER_SESSION = 50;

    /**
     * 每次召回的最大消息数
     */
    private static final int RECALL_MESSAGE_COUNT = 20;

    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (Message message : messages) {
            AiChatMessage entity = new AiChatMessage();
            entity.setSessionId(conversationId);
            entity.setRole(message.getMessageType().getValue());
            entity.setContent(message.getText());
            entity.setTokenCount(estimateTokenCount(message.getText()));
            aiChatMessageMapper.insert(entity);
        }
        // 清理超出限制的旧消息
        cleanupOldMessages(conversationId);
    }

    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, conversationId)
            .orderByDesc(AiChatMessage::getId)
            .last("LIMIT " + lastN);
        List<AiChatMessage> entities = aiChatMessageMapper.selectList(wrapper);

        List<Message> messages = new ArrayList<>(entities.size());
        // 按时间正序返回（先插入的在前面）
        for (int i = entities.size() - 1; i >= 0; i--) {
            AiChatMessage entity = entities.get(i);
            messages.add(convertToMessage(entity));
        }
        return messages;
    }

    public void clear(String conversationId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, conversationId);
        aiChatMessageMapper.delete(wrapper);
    }

    /**
     * 获取会话历史（按时间正序，默认 20 条）
     */
    public List<Message> getHistory(String conversationId) {
        return get(conversationId, RECALL_MESSAGE_COUNT);
    }

    /**
     * 保存单条用户消息
     */
    public void saveUserMessage(String conversationId, String content) {
        AiChatMessage entity = new AiChatMessage();
        entity.setSessionId(conversationId);
        entity.setRole(MessageType.USER.getValue());
        entity.setContent(content);
        entity.setTokenCount(estimateTokenCount(content));
        aiChatMessageMapper.insert(entity);
    }

    /**
     * 保存单条 AI 回复消息
     */
    public void saveAssistantMessage(String conversationId, String content) {
        AiChatMessage entity = new AiChatMessage();
        entity.setSessionId(conversationId);
        entity.setRole(MessageType.ASSISTANT.getValue());
        entity.setContent(content);
        entity.setTokenCount(estimateTokenCount(content));
        aiChatMessageMapper.insert(entity);
    }

    private Message convertToMessage(AiChatMessage entity) {
        String role = entity.getRole();
        String content = entity.getContent();
        return switch (role) {
            case "system" -> new SystemMessage(content);
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            default -> new UserMessage(content);
        };
    }

    private void cleanupOldMessages(String conversationId) {
        LambdaQueryWrapper<AiChatMessage> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(AiChatMessage::getSessionId, conversationId);
        long count = aiChatMessageMapper.selectCount(countWrapper);
        if (count > MAX_MESSAGES_PER_SESSION) {
            long deleteCount = count - MAX_MESSAGES_PER_SESSION;
            LambdaQueryWrapper<AiChatMessage> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AiChatMessage::getSessionId, conversationId)
                .orderByAsc(AiChatMessage::getId)
                .last("LIMIT " + deleteCount);
            aiChatMessageMapper.delete(deleteWrapper);
            log.info("会话 [{}] 消息超出限制，清理了 {} 条旧消息", conversationId, deleteCount);
        }
    }

    /**
     * 简单估算 Token 数（中文约 1 字 ≈ 0.6 Token）
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int chineseCount = 0;
        int otherCount = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseCount++;
            } else {
                otherCount++;
            }
        }
        return (int) (chineseCount * 0.6 + otherCount * 0.3);
    }
}
