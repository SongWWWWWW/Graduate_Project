package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dto.resp.AdminAiMessageRespDto;
import io.github.xxyopen.novel.dto.resp.AdminAiSessionRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/ai")
@RequiredArgsConstructor
@Tag(name = "AdminAiController", description = "AI 管理")
public class AdminAiController {

    private final AdminService adminService;

    @Operation(summary = "AI 会话列表")
    @GetMapping("/session/list")
    public RestResp<List<AdminAiSessionRespDto>> listAiSessions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        return adminService.listAiSessions(pageNum, pageSize);
    }

    @Operation(summary = "会话消息详情")
    @GetMapping("/session/{sessionId}/messages")
    public RestResp<List<AdminAiMessageRespDto>> listAiMessages(
            @PathVariable String sessionId) {
        return adminService.listAiMessages(sessionId);
    }

    @Operation(summary = "AI 角色列表")
    @GetMapping("/character/list")
    public RestResp<List<AiCharacter>> listAiCharacters() {
        return adminService.listAiCharacters();
    }

    @Operation(summary = "新增/更新 AI 角色")
    @PostMapping("/character")
    public RestResp<Void> saveAiCharacter(@RequestBody AiCharacter character) {
        return adminService.saveAiCharacter(character);
    }

    @Operation(summary = "删除 AI 角色")
    @DeleteMapping("/character/{id}")
    public RestResp<Void> deleteAiCharacter(@PathVariable Long id) {
        return adminService.deleteAiCharacter(id);
    }
}
