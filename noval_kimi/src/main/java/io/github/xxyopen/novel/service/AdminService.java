package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dao.entity.AuditSensitiveWord;
import io.github.xxyopen.novel.dto.req.AdminLoginReqDto;
import io.github.xxyopen.novel.dto.resp.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {
    RestResp<AdminLoginRespDto> login(AdminLoginReqDto dto);
    RestResp<AdminLoginRespDto> profile(String token);
    RestResp<List<AdminUserRespDto>> listUsers(Integer pageNum, Integer pageSize, String keyword);
    RestResp<Void> updateUserStatus(Long id, Integer status);
    RestResp<List<AdminAuthorRespDto>> listAuthors(Integer pageNum, Integer pageSize, String keyword);
    RestResp<Void> updateAuthorStatus(Long id, Integer status);
    RestResp<List<AdminBookRespDto>> listBooks(Integer pageNum, Integer pageSize, String keyword);
    RestResp<Void> updateBookStatus(Long id, Integer status);
    RestResp<Void> deleteBook(Long id);
    RestResp<List<AdminChapterRespDto>> listChapters(Long bookId, Integer pageNum, Integer pageSize);
    RestResp<Void> deleteChapter(Long id);
    RestResp<List<AdminCommentRespDto>> listComments(Integer pageNum, Integer pageSize, Integer auditStatus);
    RestResp<Void> deleteComment(Long id);
    RestResp<Void> auditComment(Long id, Integer auditStatus);
    RestResp<List<AdminAiSessionRespDto>> listAiSessions(Integer pageNum, Integer pageSize);
    RestResp<List<AdminAiMessageRespDto>> listAiMessages(String sessionId);
    RestResp<List<AiCharacter>> listAiCharacters();
    RestResp<Void> saveAiCharacter(AiCharacter character);
    RestResp<Void> deleteAiCharacter(Long id);
    RestResp<List<AdminAuditLogRespDto>> listAuditLogs(Integer pageNum, Integer pageSize, String bizType);
    RestResp<AdminAuditLogRespDto> getAuditLogDetail(Long id);
    RestResp<Void> updateAuditLogStatus(Long id, Integer status, String remark);
    RestResp<List<AdminSensitiveWordRespDto>> listSensitiveWords();
    RestResp<Void> addSensitiveWord(AuditSensitiveWord word);
    RestResp<Void> deleteSensitiveWord(Long id);
    RestResp<AdminStatsRespDto> getDashboardStats();
    RestResp<List<AdminSysLogRespDto>> listSysLogs(Integer pageNum, Integer pageSize);

    RestResp<EpubPreviewRespDto> previewEpub(MultipartFile file);

    RestResp<Void> importEpub(MultipartFile file, Long categoryId, String categoryName,
                              Integer workDirection, Long authorId, String authorName,
                              String bookName, String bookDesc);

    RestResp<AdminAuthorCreateRespDto> quickCreateAuthor(String penName, String telPhone);
}
