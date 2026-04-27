package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.dto.resp.AdminAuthorRespDto;
import io.github.xxyopen.novel.dto.resp.AdminUserRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/user")
@RequiredArgsConstructor
@Tag(name = "AdminUserController", description = "用户管理")
public class AdminUserController {

    private final AdminService adminService;

    @Operation(summary = "读者用户列表")
    @GetMapping("/list")
    public RestResp<List<AdminUserRespDto>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        return adminService.listUsers(pageNum, pageSize, keyword);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public RestResp<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return adminService.updateUserStatus(id, status);
    }

    @Operation(summary = "作家列表")
    @GetMapping("/author/list")
    public RestResp<List<AdminAuthorRespDto>> listAuthors(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        return adminService.listAuthors(pageNum, pageSize, keyword);
    }

    @Operation(summary = "更新作家状态")
    @PutMapping("/author/{id}/status")
    public RestResp<Void> updateAuthorStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return adminService.updateAuthorStatus(id, status);
    }

    @Operation(summary = "快速创建作者")
    @PostMapping("/author/quick-create")
    public RestResp<io.github.xxyopen.novel.dto.resp.AdminAuthorCreateRespDto> quickCreateAuthor(
            @Parameter(description = "笔名") @RequestParam String penName,
            @Parameter(description = "手机号") @RequestParam(required = false) String telPhone) {
        return adminService.quickCreateAuthor(penName, telPhone);
    }
}
