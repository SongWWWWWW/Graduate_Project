package io.github.xxyopen.novel.controller.front;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.req.BookAskReqDto;
import io.github.xxyopen.novel.dto.req.SemanticSearchReqDto;
import io.github.xxyopen.novel.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.dto.resp.SemanticSearchRespDto;
import io.github.xxyopen.novel.service.AiSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 前台门户-AI智能搜索控制器
 * 提供语义搜索和 RAG 问答功能
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Tag(name = "AiSearchController", description = "AI智能搜索")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_URL_PREFIX + "/ai")
@RequiredArgsConstructor
public class AiSearchController {

    private final AiSearchService aiSearchService;

    /**
     * 语义搜索小说
     * 根据描述性语句返回相关小说（如"热血玄幻升级流"）
     */
    @Operation(summary = "语义搜索小说")
    @PostMapping("/search")
    public RestResp<List<SemanticSearchRespDto>> semanticSearch(
            @RequestBody SemanticSearchReqDto req) {
        return aiSearchService.semanticSearch(req);
    }

    /**
     * RAG 问答：基于小说内容回答问题
     */
    @Operation(summary = "小说内容问答（SSE流式）")
    @PostMapping(value = "/ask", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askAboutBook(@RequestBody BookAskReqDto req) {
        return aiSearchService.askAboutBook(req);
    }

    /**
     * 相似小说推荐
     * 根据当前小说ID推荐相似小说
     */
    @Operation(summary = "相似小说推荐")
    @GetMapping("/similar/{bookId}")
    public RestResp<List<BookInfoRespDto>> similarBooks(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "5") Integer topK) {
        return aiSearchService.similarBooks(bookId, topK);
    }
}
