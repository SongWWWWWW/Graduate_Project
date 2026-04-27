package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dao.entity.AiChatSession;
import io.github.xxyopen.novel.core.audit.AiAuditResult;
import io.github.xxyopen.novel.dto.req.AiCharacterReqDto;
import io.github.xxyopen.novel.dto.req.AiChatReqDto;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 服务接口
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
public interface AiService {

    /**
     * 流式 AI 对话/续写
     *
     * @param dto 请求参数
     * @return SSE 流式响应
     */
    Flux<String> streamChat(AiChatReqDto dto);

    /**
     * 保存或更新 AI 角色设定
     */
    RestResp<Void> saveOrUpdateCharacter(AiCharacterReqDto dto);

    /**
     * 查询当前用户的角色设定列表
     */
    RestResp<List<AiCharacter>> listCharacters(Long bookId);

    /**
     * 删除角色设定
     */
    RestResp<Void> deleteCharacter(Long characterId);

    /**
     * 查询会话列表
     */
    RestResp<List<AiChatSession>> listSessions(Integer sessionType);

    /**
     * 删除会话及历史消息
     */
    RestResp<Void> deleteSession(String sessionId);

    /**
     * 生成小说大纲（非流式）
     */
    RestResp<String> generateOutline(String theme);

    /**
     * LLM 内容审核
     * @param text 待审核文本
     * @param bizType 业务类型: BOOK_CHAPTER / COMMENT / BOOK_INFO / AI_OUTPUT
     * @return 审核结果
     */
    AiAuditResult auditContent(String text, String bizType);
}
