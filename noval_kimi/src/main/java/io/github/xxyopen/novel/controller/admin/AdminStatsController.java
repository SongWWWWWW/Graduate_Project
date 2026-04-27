package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.AdminStatsRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/stats")
@RequiredArgsConstructor
@Tag(name = "AdminStatsController", description = "数据统计")
public class AdminStatsController {

    private final AdminService adminService;

    @Operation(summary = "仪表盘统计数据")
    @GetMapping("/dashboard")
    public RestResp<AdminStatsRespDto> dashboard() {
        return adminService.getDashboardStats();
    }
}
