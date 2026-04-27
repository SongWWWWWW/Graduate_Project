package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.AdminCommentRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/comment")
@RequiredArgsConstructor
@Tag(name = "AdminCommentController", description = "评论管理")
public class AdminCommentController {

    private final AdminService adminService;

    @Operation(summary = "评论列表")
    @GetMapping("/list")
    public RestResp<List<AdminCommentRespDto>> listComments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer auditStatus) {
        return adminService.listComments(pageNum, pageSize, auditStatus);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public RestResp<Void> deleteComment(@PathVariable Long id) {
        return adminService.deleteComment(id);
    }

    @Operation(summary = "审核评论")
    @PutMapping("/{id}/audit")
    public RestResp<Void> auditComment(
            @PathVariable Long id,
            @RequestParam Integer auditStatus) {
        return adminService.auditComment(id, auditStatus);
    }
}
