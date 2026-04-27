package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dto.req.BookAskReqDto;
import io.github.xxyopen.novel.dto.req.SemanticSearchReqDto;
import io.github.xxyopen.novel.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.dto.resp.SemanticSearchRespDto;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 搜索服务接口
 * 语义搜索 + RAG 问答
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
public interface AiSearchService {

    /**
     * 语义搜索小说
     */
    RestResp<List<SemanticSearchRespDto>> semanticSearch(SemanticSearchReqDto req);

    /**
     * 基于小说内容的 RAG 问答（流式）
     */
    Flux<String> askAboutBook(BookAskReqDto req);

    /**
     * 相似小说推荐
     */
    RestResp<List<BookInfoRespDto>> similarBooks(Long bookId, Integer topK);
}
