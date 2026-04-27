package io.github.xxyopen.novel.controller.admin;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.EpubPreviewRespDto;
import io.github.xxyopen.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 书籍导入管理
 */
@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX + "/import")
@RequiredArgsConstructor
@Tag(name = "AdminBookImportController", description = "书籍导入")
public class AdminBookImportController {

    private final AdminService adminService;

    @Operation(summary = "预览 EPUB 文件")
    @PostMapping("/epub/preview")
    public RestResp<EpubPreviewRespDto> previewEpub(
            @Parameter(description = "EPUB 文件") @RequestParam("file") MultipartFile file) {
        return adminService.previewEpub(file);
    }

    @Operation(summary = "确认导入 EPUB")
    @PostMapping("/epub/confirm")
    public RestResp<Void> confirmImportEpub(
            @Parameter(description = "EPUB 文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "分类ID") @RequestParam Long categoryId,
            @Parameter(description = "分类名") @RequestParam String categoryName,
            @Parameter(description = "作品方向;0-男频 1-女频") @RequestParam Integer workDirection,
            @Parameter(description = "作者ID") @RequestParam Long authorId,
            @Parameter(description = "作者名") @RequestParam String authorName,
            @Parameter(description = "书名（可覆盖）") @RequestParam(required = false) String bookName,
            @Parameter(description = "简介（可覆盖）") @RequestParam(required = false) String bookDesc) {
        return adminService.importEpub(file, categoryId, categoryName, workDirection,
                authorId, authorName, bookName, bookDesc);
    }
}
