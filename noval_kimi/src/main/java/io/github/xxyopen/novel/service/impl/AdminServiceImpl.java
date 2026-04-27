package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.util.JwtUtils;
import io.github.xxyopen.novel.dao.entity.*;
import io.github.xxyopen.novel.dao.mapper.*;
import io.github.xxyopen.novel.dto.req.AdminLoginReqDto;
import io.github.xxyopen.novel.dto.resp.*;
import io.github.xxyopen.novel.service.AdminService;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final AuthorInfoMapper authorInfoMapper;
    private final UserInfoMapper userInfoMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookContentMapper bookContentMapper;
    private final BookChapterMapper bookChapterMapper;
    private final BookCommentMapper bookCommentMapper;
    private final AuditLogMapper auditLogMapper;
    private final AuditSensitiveWordMapper sensitiveWordMapper;

    private final JwtUtils jwtUtils;

    @Override
    public RestResp<AdminLoginRespDto> login(AdminLoginReqDto dto) {
        String md5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
            .eq(SysUser::getUsername, dto.getUsername())
            .eq(SysUser::getPassword, md5));
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }
        Long roleCount = sysUserRoleMapper.selectCount(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()));
        if (roleCount == null || roleCount == 0) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }
        String token = jwtUtils.generateToken(user.getId(), SystemConfigConsts.NOVEL_ADMIN_KEY);
        SysUserRole ur = sysUserRoleMapper.selectOne(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()));
        SysRole role = ur != null ? sysRoleMapper.selectById(ur.getRoleId()) : null;
        AdminLoginRespDto resp = new AdminLoginRespDto();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRoleName(role != null ? role.getRoleName() : "管理员");
        return RestResp.ok(resp);
    }

    @Override
    public RestResp<AdminLoginRespDto> profile(String token) {
        Long userId = jwtUtils.parseToken(token, SystemConfigConsts.NOVEL_ADMIN_KEY);
        if (userId == null) throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        SysRole role = null;
        SysUserRole ur = sysUserRoleMapper.selectOne(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (ur != null) role = sysRoleMapper.selectById(ur.getRoleId());
        AdminLoginRespDto resp = new AdminLoginRespDto();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRoleName(role != null ? role.getRoleName() : "管理员");
        return RestResp.ok(resp);
    }

    @Override
    public RestResp<List<AdminUserRespDto>> listUsers(Integer pageNum, Integer pageSize, String keyword) {
        Page<UserInfo> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<UserInfo>lambdaQuery().orderByDesc(UserInfo::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(UserInfo::getUsername, keyword).or().like(UserInfo::getNickName, keyword));
        }
        Page<UserInfo> result = userInfoMapper.selectPage(page, qw);
        List<AdminUserRespDto> list = result.getRecords().stream().map(u -> {
            AdminUserRespDto d = new AdminUserRespDto();
            d.setId(u.getId());
            d.setUsername(u.getUsername());
            d.setNickName(u.getNickName());
            d.setCreateTime(format(u.getCreateTime()));
            d.setStatus(u.getStatus());
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> updateUserStatus(Long id, Integer status) {
        UserInfo u = new UserInfo(); u.setId(id); u.setStatus(status);
        userInfoMapper.updateById(u);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminAuthorRespDto>> listAuthors(Integer pageNum, Integer pageSize, String keyword) {
        Page<AuthorInfo> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<AuthorInfo>lambdaQuery().orderByDesc(AuthorInfo::getCreateTime);
        if (keyword != null && !keyword.isBlank()) qw.like(AuthorInfo::getPenName, keyword);
        Page<AuthorInfo> result = authorInfoMapper.selectPage(page, qw);
        List<AdminAuthorRespDto> list = result.getRecords().stream().map(a -> {
            AdminAuthorRespDto d = new AdminAuthorRespDto();
            d.setId(a.getId()); d.setPenName(a.getPenName()); d.setTelPhone(a.getTelPhone());
            d.setStatus(a.getStatus()); d.setCreateTime(format(a.getCreateTime()));
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> updateAuthorStatus(Long id, Integer status) {
        AuthorInfo a = new AuthorInfo(); a.setId(id); a.setStatus(status);
        authorInfoMapper.updateById(a);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminBookRespDto>> listBooks(Integer pageNum, Integer pageSize, String keyword) {
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<BookInfo>lambdaQuery().orderByDesc(BookInfo::getCreateTime);
        if (keyword != null && !keyword.isBlank()) qw.like(BookInfo::getBookName, keyword);
        Page<BookInfo> result = bookInfoMapper.selectPage(page, qw);
        List<AdminBookRespDto> list = result.getRecords().stream().map(b -> {
            AdminBookRespDto d = new AdminBookRespDto();
            d.setId(b.getId()); d.setBookName(b.getBookName()); d.setPicUrl(b.getPicUrl());
            d.setAuthorId(b.getAuthorId()); d.setAuthorName(b.getAuthorName());
            d.setCategoryName(b.getCategoryName());
            d.setWordCount(b.getWordCount()); d.setVisitCount(b.getVisitCount());
            d.setScore(b.getScore() != null ? b.getScore().floatValue() : 0f);
            d.setBookStatus(b.getBookStatus());
            d.setCreateTime(format(b.getCreateTime()));
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> updateBookStatus(Long id, Integer status) {
        BookInfo b = new BookInfo(); b.setId(id); b.setBookStatus(status);
        bookInfoMapper.updateById(b);
        return RestResp.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteBook(Long id) {
        // 1. 删除章节内容
        var chapterIds = bookChapterMapper.selectList(
            Wrappers.<BookChapter>lambdaQuery().eq(BookChapter::getBookId, id))
            .stream().map(BookChapter::getId).toList();
        if (!chapterIds.isEmpty()) {
            bookContentMapper.delete(
                Wrappers.<BookContent>lambdaQuery().in(BookContent::getChapterId, chapterIds));
        }
        // 2. 删除章节
        bookChapterMapper.delete(
            Wrappers.<BookChapter>lambdaQuery().eq(BookChapter::getBookId, id));
        // 3. 删除评论
        bookCommentMapper.delete(
            Wrappers.<BookComment>lambdaQuery().eq(BookComment::getBookId, id));
        // 4. 删除小说
        bookInfoMapper.deleteById(id);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminChapterRespDto>> listChapters(Long bookId, Integer pageNum, Integer pageSize) {
        Page<BookChapter> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<BookChapter>lambdaQuery()
            .eq(BookChapter::getBookId, bookId).orderByAsc(BookChapter::getChapterNum);
        Page<BookChapter> result = bookChapterMapper.selectPage(page, qw);
        List<AdminChapterRespDto> list = result.getRecords().stream().map(c -> {
            AdminChapterRespDto d = new AdminChapterRespDto();
            d.setId(c.getId()); d.setChapterName(c.getChapterName());
            d.setChapterNum(c.getChapterNum()); d.setWordCount(c.getWordCount());
            d.setIsVip(c.getIsVip());
            d.setUpdateTime(format(c.getUpdateTime()));
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> deleteChapter(Long id) {
        BookChapter c = bookChapterMapper.selectById(id);
        if (c != null) {
            bookChapterMapper.deleteById(id);
            bookContentMapper.deleteById(id);
        }
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminCommentRespDto>> listComments(Integer pageNum, Integer pageSize, Integer auditStatus) {
        Page<BookComment> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<BookComment>lambdaQuery().orderByDesc(BookComment::getCreateTime);
        if (auditStatus != null) qw.eq(BookComment::getAuditStatus, auditStatus);
        Page<BookComment> result = bookCommentMapper.selectPage(page, qw);
        // 批量查询章节名
        List<Long> chapterIds = result.getRecords().stream()
            .map(BookComment::getChapterId).filter(id -> id != null && id > 0).distinct().toList();
        Map<Long, String> chapterNameMap;
        if (!chapterIds.isEmpty()) {
            List<BookChapter> chapters = bookChapterMapper.selectBatchIds(chapterIds);
            chapterNameMap = chapters.stream().collect(Collectors.toMap(BookChapter::getId, BookChapter::getChapterName));
        } else {
            chapterNameMap = new HashMap<>();
        }
        List<AdminCommentRespDto> list = result.getRecords().stream().map(c -> {
            AdminCommentRespDto d = new AdminCommentRespDto();
            d.setId(c.getId()); d.setUserId(c.getUserId()); d.setBookId(c.getBookId());
            d.setChapterId(c.getChapterId());
            d.setChapterName(c.getChapterId() != null && c.getChapterId() > 0 ? chapterNameMap.get(c.getChapterId()) : null);
            d.setCommentContent(c.getCommentContent()); d.setAuditStatus(c.getAuditStatus());
            d.setCreateTime(format(c.getCreateTime()));
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> deleteComment(Long id) {
        bookCommentMapper.deleteById(id);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> auditComment(Long id, Integer auditStatus) {
        BookComment c = new BookComment(); c.setId(id); c.setAuditStatus(auditStatus);
        bookCommentMapper.updateById(c);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminAiSessionRespDto>> listAiSessions(Integer pageNum, Integer pageSize) {
        return RestResp.ok(java.util.Collections.emptyList());
    }

    @Override
    public RestResp<List<AdminAiMessageRespDto>> listAiMessages(String sessionId) {
        return RestResp.ok(java.util.Collections.emptyList());
    }

    @Override
    public RestResp<List<AiCharacter>> listAiCharacters() {
        return RestResp.ok(java.util.Collections.emptyList());
    }

    @Override
    public RestResp<Void> saveAiCharacter(AiCharacter character) {
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteAiCharacter(Long id) {
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminAuditLogRespDto>> listAuditLogs(Integer pageNum, Integer pageSize, String bizType) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        var qw = Wrappers.<AuditLog>lambdaQuery().orderByDesc(AuditLog::getCreateTime);
        if (bizType != null && !bizType.isBlank()) qw.eq(AuditLog::getBizType, bizType);
        Page<AuditLog> result = auditLogMapper.selectPage(page, qw);
        List<AdminAuditLogRespDto> list = result.getRecords().stream().map(l -> {
            AdminAuditLogRespDto d = new AdminAuditLogRespDto();
            d.setId(l.getId()); d.setBizType(l.getBizType()); d.setBizId(l.getBizId());
            d.setContent(l.getContent()); d.setLocalResult(l.getLocalResult()); d.setLocalHits(l.getLocalHits());
            d.setFinalResult(l.getFinalResult()); d.setStatus(l.getStatus() != null ? l.getStatus().intValue() : null);
            d.setOperatorId(l.getOperatorId());
            d.setCreateTime(format(l.getCreateTime()));
            // 章节审核关联查询作者名（bizId 可能是 bookId 或 chapterId）
            if ("BOOK_CHAPTER".equals(l.getBizType()) && l.getBizId() != null) {
                Long bookId = l.getBizId();
                BookInfo book = bookInfoMapper.selectById(bookId);
                // 如果查不到小说，尝试作为 chapterId 查询
                if (book == null) {
                    BookChapter chapter = bookChapterMapper.selectById(bookId);
                    if (chapter != null && chapter.getBookId() != null) {
                        book = bookInfoMapper.selectById(chapter.getBookId());
                    }
                }
                if (book != null && book.getAuthorId() != null) {
                    AuthorInfo author = authorInfoMapper.selectById(book.getAuthorId());
                    if (author != null) {
                        d.setAuthorName(author.getPenName());
                    }
                }
            }
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<AdminAuditLogRespDto> getAuditLogDetail(Long id) {
        AuditLog log = auditLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
        AdminAuditLogRespDto d = new AdminAuditLogRespDto();
        d.setId(log.getId()); d.setBizType(log.getBizType()); d.setBizId(log.getBizId());
        d.setContent(log.getContent()); d.setLocalResult(log.getLocalResult()); d.setLocalHits(log.getLocalHits());
        d.setFinalResult(log.getFinalResult()); d.setStatus(log.getStatus() != null ? log.getStatus().intValue() : null);
        d.setOperatorId(log.getOperatorId());
        d.setCreateTime(format(log.getCreateTime()));
        // 如果内容是章节且被截断了，从 book_content 表补全完整内容
        if ("BOOK_CHAPTER".equals(log.getBizType()) && log.getBizId() != null) {
            BookContent bc = bookContentMapper.selectOne(
                Wrappers.<BookContent>lambdaQuery().eq(BookContent::getChapterId, log.getBizId()));
            if (bc != null && bc.getContent() != null && !bc.getContent().isBlank()) {
                d.setContent(bc.getContent());
            }
        }
        return RestResp.ok(d);
    }

    @Override
    public RestResp<Void> updateAuditLogStatus(Long id, Integer status, String remark) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setStatus(status.byteValue());
        log.setOperatorRemark(remark);
        log.setUpdateTime(LocalDateTime.now());
        auditLogMapper.updateById(log);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<AdminSensitiveWordRespDto>> listSensitiveWords() {
        List<AuditSensitiveWord> words = sensitiveWordMapper.selectList(Wrappers.emptyWrapper());
        List<AdminSensitiveWordRespDto> list = words.stream().map(w -> {
            AdminSensitiveWordRespDto d = new AdminSensitiveWordRespDto();
            d.setId(w.getId()); d.setWord(w.getWord()); d.setCategory(w.getCategory());
            d.setEnabled(w.getIsEnabled() != null ? w.getIsEnabled() : 1);
            d.setCreateTime(format(w.getCreateTime()));
            return d;
        }).collect(Collectors.toList());
        return RestResp.ok(list);
    }

    @Override
    public RestResp<Void> addSensitiveWord(AuditSensitiveWord word) {
        word.setCreateTime(LocalDateTime.now());
        sensitiveWordMapper.insert(word);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteSensitiveWord(Long id) {
        sensitiveWordMapper.deleteById(id);
        return RestResp.ok();
    }

    @Override
    public RestResp<AdminStatsRespDto> getDashboardStats() {
        AdminStatsRespDto d = new AdminStatsRespDto();
        d.setUserCount(sysUserMapper.selectCount(Wrappers.emptyWrapper()));
        d.setAuthorCount(authorInfoMapper.selectCount(Wrappers.emptyWrapper()));
        d.setBookCount(bookInfoMapper.selectCount(Wrappers.emptyWrapper()));
        d.setCommentCount(bookCommentMapper.selectCount(Wrappers.emptyWrapper()));
        d.setTodayNewUsers(0L); d.setTodayNewBooks(0L);
        return RestResp.ok(d);
    }

    @Override
    public RestResp<List<AdminSysLogRespDto>> listSysLogs(Integer pageNum, Integer pageSize) {
        return RestResp.ok(java.util.Collections.emptyList());
    }

    @Override
    public RestResp<EpubPreviewRespDto> previewEpub(MultipartFile file) {
        try {
            EpubMeta meta = parseEpub(file.getBytes());
            EpubPreviewRespDto dto = new EpubPreviewRespDto();
            dto.setBookName(meta.title);
            dto.setAuthor(meta.author);
            dto.setDescription(meta.description);
            dto.setChapters(meta.chapters);
            dto.setTotalChapters(meta.chapters.size());
            dto.setTotalWords(meta.chapters.stream().mapToInt(EpubChapterRespDto::getWordCount).sum());
            return RestResp.ok(dto);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> importEpub(MultipartFile file, Long categoryId, String categoryName,
                                      Integer workDirection, Long authorId, String authorName,
                                      String bookName, String bookDesc) {
        try {
            EpubMeta meta = parseEpub(file.getBytes());
            if (meta.chapters.isEmpty()) {
                throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
            }

            // 1. 插入 book_info
            BookInfo book = new BookInfo();
            book.setWorkDirection(workDirection);
            book.setCategoryId(categoryId);
            book.setCategoryName(categoryName);
            book.setPicUrl("/default.jpg");
            book.setBookName(bookName != null && !bookName.isBlank() ? bookName : meta.title);
            book.setAuthorId(authorId);
            book.setAuthorName(authorName);
            book.setBookDesc(bookDesc != null && !bookDesc.isBlank() ? bookDesc : meta.description);
            book.setScore(80);
            book.setBookStatus(0);
            book.setVisitCount(0L);
            book.setWordCount(0);
            book.setCommentCount(0);
            book.setIsVip(0);
            book.setCreateTime(LocalDateTime.now());
            book.setUpdateTime(LocalDateTime.now());
            bookInfoMapper.insert(book);

            Long bookId = book.getId();
            int totalWords = 0;
            Long lastChapterId = null;
            String lastChapterName = null;

            // 2. 插入章节
            for (int i = 0; i < meta.chapters.size(); i++) {
                EpubChapterRespDto chDto = meta.chapters.get(i);
                String content = chDto.getPreview();

                BookChapter chapter = new BookChapter();
                chapter.setBookId(bookId);
                chapter.setChapterNum(i + 1);
                chapter.setChapterName(chDto.getChapterName());
                chapter.setWordCount(chDto.getWordCount());
                chapter.setIsVip(0);
                chapter.setCreateTime(LocalDateTime.now());
                chapter.setUpdateTime(LocalDateTime.now());
                bookChapterMapper.insert(chapter);

                Long chapterId = chapter.getId();
                lastChapterId = chapterId;
                lastChapterName = chapter.getChapterName();
                totalWords += chapter.getWordCount();

                BookContent bc = new BookContent();
                bc.setChapterId(chapterId);
                bc.setContent(content);
                bc.setCreateTime(LocalDateTime.now());
                bc.setUpdateTime(LocalDateTime.now());
                bookContentMapper.insert(bc);
            }

            // 3. 更新 book_info 统计
            BookInfo update = new BookInfo();
            update.setId(bookId);
            update.setWordCount(totalWords);
            update.setLastChapterId(lastChapterId);
            update.setLastChapterName(lastChapterName);
            update.setLastChapterUpdateTime(LocalDateTime.now());
            bookInfoMapper.updateById(update);

            return RestResp.ok();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
    }

    /**
     * EPUB 元数据结构
     */
    private static class EpubMeta {
        String title = "未命名";
        String author = "未知作者";
        String description = "";
        List<EpubChapterRespDto> chapters = new ArrayList<>();
    }

    /**
     * 解析 EPUB 文件（纯 ZIP + jsoup，无需 epublib）
     */
    private EpubMeta parseEpub(byte[] data) throws IOException {
        EpubMeta meta = new EpubMeta();
        Map<String, byte[]> zipEntries = new HashMap<>();

        // 1. 读取 ZIP 中所有文件
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] buf = zis.readAllBytes();
                    zipEntries.put(entry.getName(), buf);
                }
            }
        }

        // 2. 找到 container.xml -> content.opf 路径
        byte[] containerBytes = zipEntries.get("META-INF/container.xml");
        if (containerBytes == null) {
            throw new IOException("无效的 EPUB：缺少 META-INF/container.xml");
        }
        Document containerDoc = Jsoup.parse(new String(containerBytes, StandardCharsets.UTF_8), "", Parser.xmlParser());
        Element rootfile = containerDoc.selectFirst("rootfile[media-type=application/oebps-package+xml]");
        if (rootfile == null) {
            throw new IOException("无效的 EPUB：找不到 rootfile");
        }
        String opfPath = rootfile.attr("full-path");
        String opfDir = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1) : "";

        // 3. 解析 content.opf
        byte[] opfBytes = zipEntries.get(opfPath);
        if (opfBytes == null) throw new IOException("找不到 content.opf");
        Document opfDoc = Jsoup.parse(new String(opfBytes, StandardCharsets.UTF_8), "", Parser.xmlParser());

        // metadata
        Element titleEl = opfDoc.selectFirst("metadata > dc|title, metadata title");
        if (titleEl != null) meta.title = titleEl.text().trim();
        Element authorEl = opfDoc.selectFirst("metadata > dc|creator, metadata creator");
        if (authorEl != null) meta.author = authorEl.text().trim();
        Element descEl = opfDoc.selectFirst("metadata > dc|description, metadata description");
        if (descEl != null) meta.description = descEl.text().trim();

        // manifest: id -> href
        Map<String, String> manifest = new HashMap<>();
        for (Element item : opfDoc.select("manifest > item")) {
            manifest.put(item.attr("id"), item.attr("href"));
        }

        // spine: 阅读顺序
        List<String> spineIds = new ArrayList<>();
        for (Element itemref : opfDoc.select("spine > itemref")) {
            spineIds.add(itemref.attr("idref"));
        }

        // 4. 按 spine 顺序提取章节
        int chapterNum = 0;
        for (String idref : spineIds) {
            String href = manifest.get(idref);
            if (href == null) continue;
            String fullPath = opfDir + href;
            byte[] contentBytes = zipEntries.get(fullPath);
            if (contentBytes == null) continue;

            try {
                String html = new String(contentBytes, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(html);

                // 提取标题
                String title = null;
                Elements h1 = doc.select("h1");
                if (!h1.isEmpty()) title = h1.first().text();
                if (title == null || title.isBlank()) {
                    Elements h2 = doc.select("h2");
                    if (!h2.isEmpty()) title = h2.first().text();
                }
                if (title == null || title.isBlank()) {
                    Elements t = doc.select("title");
                    if (!t.isEmpty()) title = t.first().text();
                }
                if (title == null || title.isBlank()) {
                    title = "第" + (chapterNum + 1) + "章";
                }
                title = title.trim();

                // 过滤非章节内容：封面、目录、前言等
                if (isNonChapterTitle(title)) {
                    continue;
                }

                // 提取正文，保留段落结构
                doc.select("script, style, nav").remove();
                StringBuilder textBuilder = new StringBuilder();
                Element body = doc.body();
                if (body != null) {
                    // 尝试按 <p> 标签提取段落
                    Elements paragraphs = body.select("p");
                    if (paragraphs.size() > 1) {
                        for (Element p : paragraphs) {
                            String paraText = p.text().trim();
                            if (!paraText.isEmpty()) {
                                textBuilder.append(paraText).append("\n\n");
                            }
                        }
                    } else {
                        // 没有 <p> 标签，按 div 或 br 分段
                        String bodyHtml = body.html();
                        String[] parts = bodyHtml.split("<br\\s*/?>", -1);
                        for (String part : parts) {
                            String clean = Jsoup.parse(part).text().trim();
                            if (!clean.isEmpty()) {
                                textBuilder.append(clean).append("\n\n");
                            }
                        }
                    }
                }
                String text = textBuilder.toString().trim();

                // 移除标题在正文开头的情况
                if (text.startsWith(title)) {
                    text = text.substring(title.length()).trim();
                }

                // 过滤太短的页面（可能是封面、版权页等）
                if (text.length() < 50) continue;

                chapterNum++;
                EpubChapterRespDto dto = new EpubChapterRespDto();
                dto.setChapterNum(chapterNum);
                dto.setChapterName(title);
                dto.setPreview(text);
                dto.setWordCount(text.length());
                meta.chapters.add(dto);
            } catch (Exception e) {
                // 跳过解析失败的资源
            }
        }

        return meta;
    }

    /**
     * 判断标题是否为非章节内容（封面、目录、前言等）
     */
    private boolean isNonChapterTitle(String title) {
        if (title == null || title.isBlank()) return true;
        String lower = title.toLowerCase();
        String[] skipKeywords = {
            // 封面相关
            "封面", "封底", "扉页", "cover", "title page",
            // 目录相关
            "目录", "目次", "table of contents", "contents", "toc",
            // 前言相关
            "前言", "序言", "导言", "引言", "绪论", "卷首语", "开卷语",
            "preface", "foreword", "introduction", "prologue",
            // 后记相关
            "后记", "跋", "尾声", "epilogue", "afterword",
            // 附录相关
            "附录", "附", "annex", "appendix",
            // 版权相关
            "版权", "著作权", "出版说明", "版权声明", "copyright",
            // 致谢相关
            "致谢", "鸣谢", "acknowledgements", "acknowledgments",
            // 作者相关
            "关于作者", "作者简介", "作者介绍", "about the author",
            // 其他
            "内容提要", "内容摘要", "摘要", "abstract", "synopsis",
            "献词", "题词", "dedication",
        };
        for (String kw : skipKeywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<AdminAuthorCreateRespDto> quickCreateAuthor(String penName, String telPhone) {
        if (penName == null || penName.isBlank()) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
        // 生成唯一用户名（时间戳+随机数）
        String username = "author_" + System.currentTimeMillis();
        String defaultPassword = DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8));

        // 1. 创建 user_info
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(defaultPassword);
        user.setSalt("0");
        user.setNickName(penName);
        user.setUserSex(1);
        user.setAccountBalance(0L);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userInfoMapper.insert(user);

        // 2. 创建 author_info
        AuthorInfo author = new AuthorInfo();
        author.setUserId(user.getId());
        author.setInviteCode("0");
        author.setPenName(penName);
        author.setTelPhone(telPhone != null ? telPhone : "");
        author.setChatAccount("");
        author.setEmail("");
        author.setWorkDirection(0);
        author.setStatus(0);
        author.setCreateTime(LocalDateTime.now());
        author.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.insert(author);

        AdminAuthorCreateRespDto dto = new AdminAuthorCreateRespDto();
        dto.setId(author.getId());
        dto.setPenName(penName);
        return RestResp.ok(dto);
    }

    private String format(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
