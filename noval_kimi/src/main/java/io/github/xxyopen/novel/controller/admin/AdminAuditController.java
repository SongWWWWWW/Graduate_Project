package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dao.entity.AuditSensitiveWord;
import io.github.xxyopen.novel.dto.resp.AdminAuditLogRespDto;
import io.github.xxyopen.novel.dto.resp.AdminSensitiveWordRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/audit")
@RequiredArgsConstructor
@Tag(name = "AdminAuditController", description = "审核管理")
public class AdminAuditController {

    private final AdminService adminService;

    @Operation(summary = "审核日志列表")
    @GetMapping("/log/list")
    public RestResp<List<AdminAuditLogRespDto>> listAuditLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "业务类型") @RequestParam(required = false) String bizType) {
        return adminService.listAuditLogs(pageNum, pageSize, bizType);
    }

    @Operation(summary = "审核日志详情")
    @GetMapping("/log/{id}")
    public RestResp<AdminAuditLogRespDto> getAuditLogDetail(@PathVariable Long id) {
        return adminService.getAuditLogDetail(id);
    }

    @Operation(summary = "更新审核日志状态")
    @PutMapping("/log/{id}/status")
    public RestResp<Void> updateAuditLogStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        return adminService.updateAuditLogStatus(id, status, remark);
    }

    @Operation(summary = "敏感词列表")
    @GetMapping("/sensitive/list")
    public RestResp<List<AdminSensitiveWordRespDto>> listSensitiveWords() {
        return adminService.listSensitiveWords();
    }

    @Operation(summary = "新增敏感词")
    @PostMapping("/sensitive")
    public RestResp<Void> addSensitiveWord(@RequestBody AuditSensitiveWord word) {
        return adminService.addSensitiveWord(word);
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/sensitive/{id}")
    public RestResp<Void> deleteSensitiveWord(@PathVariable Long id) {
        return adminService.deleteSensitiveWord(id);
    }
}
