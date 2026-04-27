package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.core.util.JwtUtils;
import io.github.xxyopen.novel.dto.req.AdminLoginReqDto;
import io.github.xxyopen.novel.dto.resp.AdminLoginRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX)
@RequiredArgsConstructor
@Tag(name = "AdminController", description = "管理员认证")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public RestResp<AdminLoginRespDto> login(@Valid @RequestBody AdminLoginReqDto dto) {
        return adminService.login(dto);
    }

    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/profile")
    public RestResp<AdminLoginRespDto> profile(
            @RequestHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME) String token) {
        return adminService.profile(token);
    }
}
