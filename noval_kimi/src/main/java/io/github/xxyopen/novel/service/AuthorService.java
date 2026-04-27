package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.req.PageReqDto;
import io.github.xxyopen.novel.core.common.resp.PageRespDto;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dto.req.AuthorProfileUpdateReqDto;
import io.github.xxyopen.novel.dto.req.AuthorRegisterReqDto;
import io.github.xxyopen.novel.dto.resp.*;

/**
 * 作家模块 业务服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/23
 */
public interface AuthorService {

    /**
     * 作家注册
     *
     * @param dto 注册参数
     * @return void
     */
    RestResp<Void> register(AuthorRegisterReqDto dto);

    /**
     * 查询作家状态
     *
     * @param userId 用户ID
     * @return 作家状态
     */
    RestResp<Integer> getStatus(Long userId);

    /**
     * 查询作家工作台数据
     *
     * @return 工作台数据
     */
    RestResp<AuthorDashboardRespDto> getDashboard();

    /**
     * 查询作家作品统计列表
     *
     * @param pageReqDto 分页参数
     * @return 作品统计列表
     */
    RestResp<PageRespDto<AuthorBookStatsRespDto>> listBookStats(PageReqDto pageReqDto);

    /**
     * 查询作家稿费收入列表
     *
     * @param pageReqDto 分页参数
     * @return 稿费收入列表
     */
    RestResp<PageRespDto<AuthorIncomeRespDto>> listIncome(PageReqDto pageReqDto);

    /**
     * 查询作家稿费收入明细列表
     *
     * @param bookId 小说ID
     * @param pageReqDto 分页参数
     * @return 稿费收入明细列表
     */
    RestResp<PageRespDto<AuthorIncomeDetailRespDto>> listIncomeDetail(Long bookId, PageReqDto pageReqDto);

    /**
     * 查询作家作品评论列表
     *
     * @param bookId 小说ID
     * @param pageReqDto 分页参数
     * @return 评论列表
     */
    RestResp<PageRespDto<AuthorBookCommentRespDto>> listBookComments(Long bookId, PageReqDto pageReqDto);

    /**
     * 查询作家资料
     *
     * @return 作家资料
     */
    RestResp<AuthorProfileRespDto> getProfile();

    /**
     * 更新作家资料
     *
     * @param dto 更新参数
     * @return void
     */
    RestResp<Void> updateProfile(AuthorProfileUpdateReqDto dto);
}
