package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.AdminSysLogRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/sys")
@RequiredArgsConstructor
@Tag(name = "AdminSysController", description = "系统管理")
public class AdminSysController {

    private final AdminService adminService;

    @Operation(summary = "系统日志列表")
    @GetMapping("/log/list")
    public RestResp<List<AdminSysLogRespDto>> listSysLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        return adminService.listSysLogs(pageNum, pageSize);
    }
}
