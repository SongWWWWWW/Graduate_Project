package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.AdminBookRespDto;
import io.github.xxyopen.novel.dto.resp.AdminChapterRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/book")
@RequiredArgsConstructor
@Tag(name = "AdminBookController", description = "小说管理")
public class AdminBookController {

    private final AdminService adminService;

    @Operation(summary = "小说列表")
    @GetMapping("/list")
    public RestResp<List<AdminBookRespDto>> listBooks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        return adminService.listBooks(pageNum, pageSize, keyword);
    }

    @Operation(summary = "更新小说状态")
    @PutMapping("/{id}/status")
    public RestResp<Void> updateBookStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return adminService.updateBookStatus(id, status);
    }

    @Operation(summary = "删除小说")
    @DeleteMapping("/{id}")
    public RestResp<Void> deleteBook(@PathVariable Long id) {
        return adminService.deleteBook(id);
    }

    @Operation(summary = "章节列表")
    @GetMapping("/{bookId}/chapters")
    public RestResp<List<AdminChapterRespDto>> listChapters(
            @PathVariable Long bookId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        return adminService.listChapters(bookId, pageNum, pageSize);
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/chapter/{id}")
    public RestResp<Void> deleteChapter(@PathVariable Long id) {
        return adminService.deleteChapter(id);
    }
}
