package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xxyopen.novel.core.auth.UserHolder;
import io.github.xxyopen.novel.core.common.req.PageReqDto;
import io.github.xxyopen.novel.core.common.resp.PageRespDto;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.DatabaseConsts;
import io.github.xxyopen.novel.dao.entity.*;
import io.github.xxyopen.novel.dao.mapper.*;
import io.github.xxyopen.novel.dto.AuthorInfoDto;
import io.github.xxyopen.novel.dto.req.AuthorProfileUpdateReqDto;
import io.github.xxyopen.novel.dto.req.AuthorRegisterReqDto;
import io.github.xxyopen.novel.dto.resp.*;
import io.github.xxyopen.novel.manager.cache.AuthorInfoCacheManager;
import io.github.xxyopen.novel.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 作家模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final AuthorInfoMapper authorInfoMapper;

    private final BookInfoMapper bookInfoMapper;

    private final AuthorIncomeMapper authorIncomeMapper;

    private final AuthorIncomeDetailMapper authorIncomeDetailMapper;

    private final BookCommentMapper bookCommentMapper;

    private final UserInfoMapper userInfoMapper;

    @Override
    public RestResp<Void> register(AuthorRegisterReqDto dto) {
        // 校验该用户是否已注册为作家
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(dto.getUserId());
        if (Objects.nonNull(author)) {
            // 该用户已经是作家，直接返回
            return RestResp.ok();
        }
        // 保存作家注册信息
        AuthorInfo authorInfo = new AuthorInfo();
        authorInfo.setUserId(dto.getUserId());
        authorInfo.setChatAccount(dto.getChatAccount());
        authorInfo.setEmail(dto.getEmail());
        authorInfo.setInviteCode("0");
        authorInfo.setTelPhone(dto.getTelPhone());
        authorInfo.setPenName(dto.getPenName());
        authorInfo.setWorkDirection(dto.getWorkDirection());
        authorInfo.setCreateTime(LocalDateTime.now());
        authorInfo.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.insert(authorInfo);
        // 清除作家缓存
        authorInfoCacheManager.evictAuthorCache();
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getStatus(Long userId) {
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(userId);
        return Objects.isNull(author) ? RestResp.ok(null) : RestResp.ok(author.getStatus());
    }

    @Override
    public RestResp<AuthorDashboardRespDto> getDashboard() {
        Long authorId = UserHolder.getAuthorId();
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        String penName = author != null ? author.getPenName() : "";

        // 查询该作者的所有作品
        QueryWrapper<BookInfo> bookWrapper = new QueryWrapper<>();
        bookWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, authorId)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        List<BookInfo> books = bookInfoMapper.selectList(bookWrapper);

        int bookCount = books.size();
        int totalWordCount = books.stream().mapToInt(BookInfo::getWordCount).sum();
        long totalVisitCount = books.stream().mapToLong(BookInfo::getVisitCount).sum();
        int totalCommentCount = books.stream().mapToInt(BookInfo::getCommentCount).sum();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<AuthorDashboardRespDto.RecentBook> recentBooks = books.stream().limit(5).map(v ->
            AuthorDashboardRespDto.RecentBook.builder()
                .id(v.getId())
                .bookName(v.getBookName())
                .picUrl(v.getPicUrl())
                .wordCount(v.getWordCount())
                .visitCount(v.getVisitCount())
                .lastChapterName(v.getLastChapterName())
                .lastChapterUpdateTime(v.getLastChapterUpdateTime() != null ? v.getLastChapterUpdateTime().format(formatter) : null)
                .build()
        ).toList();

        return RestResp.ok(AuthorDashboardRespDto.builder()
            .penName(penName)
            .bookCount(bookCount)
            .totalWordCount(totalWordCount)
            .totalVisitCount(totalVisitCount)
            .totalCommentCount(totalCommentCount)
            .recentBooks(recentBooks)
            .build());
    }

    @Override
    public RestResp<PageRespDto<AuthorBookStatsRespDto>> listBookStats(PageReqDto pageReqDto) {
        Long authorId = UserHolder.getAuthorId();
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, authorId)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        IPage<BookInfo> bookInfoPage = bookInfoMapper.selectPage(page, queryWrapper);

        List<AuthorBookStatsRespDto> list = bookInfoPage.getRecords().stream().map(v ->
            AuthorBookStatsRespDto.builder()
                .id(v.getId())
                .bookName(v.getBookName())
                .picUrl(v.getPicUrl())
                .categoryName(v.getCategoryName())
                .wordCount(v.getWordCount())
                .visitCount(v.getVisitCount())
                .commentCount(v.getCommentCount())
                .bookStatus(v.getBookStatus())
                .lastChapterName(v.getLastChapterName())
                .lastChapterUpdateTime(v.getLastChapterUpdateTime())
                .createTime(v.getCreateTime())
                .build()
        ).toList();

        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<PageRespDto<AuthorIncomeRespDto>> listIncome(PageReqDto pageReqDto) {
        Long authorId = UserHolder.getAuthorId();
        IPage<AuthorIncome> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());
        QueryWrapper<AuthorIncome> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId)
            .orderByDesc("income_month");
        IPage<AuthorIncome> incomePage = authorIncomeMapper.selectPage(page, queryWrapper);

        // 批量查询书名
        List<Long> bookIds = incomePage.getRecords().stream()
            .map(AuthorIncome::getBookId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> bookNameMap = bookIds.isEmpty() ? Map.of() :
            bookInfoMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getId, BookInfo::getBookName));

        List<AuthorIncomeRespDto> list = incomePage.getRecords().stream().map(v ->
            AuthorIncomeRespDto.builder()
                .id(v.getId())
                .bookId(v.getBookId())
                .bookName(bookNameMap.getOrDefault(v.getBookId(), ""))
                .incomeMonth(v.getIncomeMonth())
                .preTaxIncome(formatFenToYuan(v.getPreTaxIncome()))
                .afterTaxIncome(formatFenToYuan(v.getAfterTaxIncome()))
                .payStatus(v.getPayStatus())
                .confirmStatus(v.getConfirmStatus())
                .detail(v.getDetail())
                .build()
        ).toList();

        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<PageRespDto<AuthorIncomeDetailRespDto>> listIncomeDetail(Long bookId, PageReqDto pageReqDto) {
        Long authorId = UserHolder.getAuthorId();
        IPage<AuthorIncomeDetail> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());
        QueryWrapper<AuthorIncomeDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        if (bookId != null && bookId > 0) {
            queryWrapper.eq("book_id", bookId);
        }
        queryWrapper.orderByDesc("income_date");
        IPage<AuthorIncomeDetail> detailPage = authorIncomeDetailMapper.selectPage(page, queryWrapper);

        // 批量查询书名
        List<Long> bookIds = detailPage.getRecords().stream()
            .map(AuthorIncomeDetail::getBookId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> bookNameMap = bookIds.isEmpty() ? Map.of() :
            bookInfoMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getId, BookInfo::getBookName));

        List<AuthorIncomeDetailRespDto> list = detailPage.getRecords().stream().map(v ->
            AuthorIncomeDetailRespDto.builder()
                .id(v.getId())
                .bookId(v.getBookId())
                .bookName(bookNameMap.getOrDefault(v.getBookId(), ""))
                .incomeDate(v.getIncomeDate())
                .incomeAccount(formatFenToYuan(v.getIncomeAccount()))
                .incomeCount(v.getIncomeCount())
                .incomeNumber(v.getIncomeNumber())
                .build()
        ).toList();

        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<PageRespDto<AuthorBookCommentRespDto>> listBookComments(Long bookId, PageReqDto pageReqDto) {
        Long authorId = UserHolder.getAuthorId();

        // 先查询该作者的所有作品ID
        QueryWrapper<BookInfo> bookWrapper = new QueryWrapper<>();
        bookWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, authorId)
            .select(DatabaseConsts.CommonColumnEnum.ID.getName(), "book_name", "pic_url");
        List<BookInfo> authorBooks = bookInfoMapper.selectList(bookWrapper);
        Map<Long, BookInfo> bookMap = authorBooks.stream()
            .collect(Collectors.toMap(BookInfo::getId, b -> b));

        if (bookMap.isEmpty()) {
            return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), 0, List.of()));
        }

        List<Long> bookIdList = bookMap.keySet().stream().collect(Collectors.toList());

        IPage<BookComment> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("book_id", bookIdList);
        if (bookId != null && bookId > 0) {
            queryWrapper.eq("book_id", bookId);
        }
        queryWrapper.orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookComment> commentPage = bookCommentMapper.selectPage(page, queryWrapper);

        // 批量查询用户信息
        List<Long> userIds = commentPage.getRecords().stream()
            .map(BookComment::getUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, UserInfo> userMap = userIds.isEmpty() ? Map.of() :
            userInfoMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, u -> u));

        List<AuthorBookCommentRespDto> list = commentPage.getRecords().stream().map(v -> {
            BookInfo book = bookMap.get(v.getBookId());
            UserInfo user = userMap.get(v.getUserId());
            return AuthorBookCommentRespDto.builder()
                .id(v.getId())
                .bookId(v.getBookId())
                .bookName(book != null ? book.getBookName() : "")
                .bookPic(book != null ? book.getPicUrl() : "")
                .userId(v.getUserId())
                .userNickName(user != null ? user.getNickName() : "")
                .userPhoto(user != null ? user.getUserPhoto() : "")
                .commentContent(v.getCommentContent())
                .replyCount(v.getReplyCount())
                .auditStatus(v.getAuditStatus())
                .createTime(v.getCreateTime())
                .build();
        }).toList();

        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<AuthorProfileRespDto> getProfile() {
        Long userId = UserHolder.getUserId();
        AuthorInfoDto authorDto = authorInfoCacheManager.getAuthor(userId);
        if (authorDto == null) {
            return RestResp.ok(null);
        }
        AuthorInfo authorInfo = authorInfoMapper.selectById(authorDto.getId());
        if (authorInfo == null) {
            return RestResp.ok(null);
        }
        return RestResp.ok(AuthorProfileRespDto.builder()
            .id(authorInfo.getId())
            .userId(authorInfo.getUserId())
            .penName(authorInfo.getPenName())
            .telPhone(authorInfo.getTelPhone())
            .chatAccount(authorInfo.getChatAccount())
            .email(authorInfo.getEmail())
            .workDirection(authorInfo.getWorkDirection())
            .status(authorInfo.getStatus())
            .createTime(authorInfo.getCreateTime())
            .build());
    }

    @Override
    public RestResp<Void> updateProfile(AuthorProfileUpdateReqDto dto) {
        Long userId = UserHolder.getUserId();
        AuthorInfoDto authorDto = authorInfoCacheManager.getAuthor(userId);
        if (authorDto == null) {
            return RestResp.ok();
        }
        AuthorInfo authorInfo = new AuthorInfo();
        authorInfo.setId(authorDto.getId());
        authorInfo.setPenName(dto.getPenName());
        authorInfo.setTelPhone(dto.getTelPhone());
        authorInfo.setChatAccount(dto.getChatAccount());
        authorInfo.setEmail(dto.getEmail());
        authorInfo.setWorkDirection(dto.getWorkDirection());
        authorInfo.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.updateById(authorInfo);
        // 清除作家缓存
        authorInfoCacheManager.evictAuthorCache();
        return RestResp.ok();
    }

    private String formatFenToYuan(Integer fen) {
        if (fen == null) return "0.00";
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString();
    }
}
