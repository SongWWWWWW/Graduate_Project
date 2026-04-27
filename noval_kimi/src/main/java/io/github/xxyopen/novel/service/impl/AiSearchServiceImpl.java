package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.entity.BookInfo;
import io.github.xxyopen.novel.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.dto.req.BookAskReqDto;
import io.github.xxyopen.novel.dto.req.SemanticSearchReqDto;
import io.github.xxyopen.novel.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.dto.resp.SemanticSearchRespDto;
import io.github.xxyopen.novel.service.AiSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 搜索服务实现
 * 支持语义搜索（Milvus向量检索）和数据库降级搜索
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Service
public class AiSearchServiceImpl implements AiSearchService {

    @Autowired(required = false)
    private VectorStore vectorStore;

    private final ChatClient chatClient;
    private final BookInfoMapper bookInfoMapper;

    public AiSearchServiceImpl(ChatClient chatClient, BookInfoMapper bookInfoMapper) {
        this.chatClient = chatClient;
        this.bookInfoMapper = bookInfoMapper;
    }

    @Override
    public RestResp<List<SemanticSearchRespDto>> semanticSearch(SemanticSearchReqDto req) {
        String query = req.getQuery();
        int topK = req.getTopK() != null ? req.getTopK() : 10;

        // Milvus 未启动时降级到数据库关键字搜索
        if (vectorStore == null) {
            log.warn("[AI搜索降级] Milvus向量数据库未启用，使用数据库关键字搜索: query={}", query);
            return dbFallbackSemanticSearch(query, topK);
        }

        // 1. 向量检索（语义相似度）
        List<Document> docs = vectorStore.similaritySearch(query);
        if (docs.size() > topK * 3) {
            docs = docs.subList(0, topK * 3);
        }

        // 3. 按 book_id 分组聚合，取最相关的章节
        Map<Long, List<Document>> bookDocMap = docs.stream()
            .filter(doc -> doc.getMetadata().get("book_id") != null)
            .collect(Collectors.groupingBy(
                doc -> Long.valueOf(doc.getMetadata().get("book_id").toString())
            ));

        // 4. 查询 MySQL 补全小说信息
        List<Long> bookIds = new ArrayList<>(bookDocMap.keySet());
        if (bookIds.isEmpty()) {
            return RestResp.ok(Collections.emptyList());
        }

        List<BookInfo> bookInfos = bookInfoMapper.selectBatchIds(bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfos.stream()
            .collect(Collectors.toMap(BookInfo::getId, b -> b));

        // 5. 组装结果
        List<SemanticSearchRespDto> results = new ArrayList<>();
        for (Map.Entry<Long, List<Document>> entry : bookDocMap.entrySet()) {
            Long bookId = entry.getKey();
            BookInfo bookInfo = bookInfoMap.get(bookId);
            if (bookInfo == null) continue;

            // 取第一个文档作为相关片段
            String snippet = entry.getValue().get(0).getText();
            if (snippet.length() > 200) {
                snippet = snippet.substring(0, 200) + "...";
            }

            results.add(SemanticSearchRespDto.builder()
                .bookId(bookId)
                .bookName(bookInfo.getBookName())
                .authorName(bookInfo.getAuthorName())
                .categoryName(bookInfo.getCategoryName())
                .bookDesc(bookInfo.getBookDesc())
                .picUrl(bookInfo.getPicUrl())
                .score(null)
                .relevantSnippet(snippet)
                .build());
        }

        // 限制返回数量
        if (results.size() > topK) {
            results = results.subList(0, topK);
        }

        return RestResp.ok(results);
    }

    /**
     * 数据库降级搜索：当 Milvus 不可用时，通过书名/简介 LIKE 匹配
     */
    private RestResp<List<SemanticSearchRespDto>> dbFallbackSemanticSearch(String keyword, int topK) {
        if (keyword == null || keyword.isBlank()) {
            return RestResp.ok(Collections.emptyList());
        }
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(BookInfo::getBookName, keyword)
            .or()
            .like(BookInfo::getBookDesc, keyword)
            .or()
            .like(BookInfo::getAuthorName, keyword)
            .orderByDesc(BookInfo::getVisitCount)
            .last("LIMIT " + topK);

        List<BookInfo> books = bookInfoMapper.selectList(wrapper);
        List<SemanticSearchRespDto> results = books.stream().map(b -> SemanticSearchRespDto.builder()
            .bookId(b.getId())
            .bookName(b.getBookName())
            .authorName(b.getAuthorName())
            .categoryName(b.getCategoryName())
            .bookDesc(b.getBookDesc())
            .picUrl(b.getPicUrl())
            .score(null)
            .relevantSnippet(b.getBookDesc() != null && b.getBookDesc().length() > 200
                ? b.getBookDesc().substring(0, 200) + "..."
                : b.getBookDesc())
            .build()).toList();

        return RestResp.ok(results);
    }

    @Override
    public Flux<String> askAboutBook(BookAskReqDto req) {
        String question = req.getQuestion();
        Long bookId = req.getBookId();
        int topK = req.getTopK() != null ? req.getTopK() : 5;

        // Milvus 未启动时返回降级提示
        if (vectorStore == null) {
            log.warn("[AI问答降级] Milvus向量数据库未启用，无法检索小说内容");
            return Flux.just(
                "抱歉，智能问答服务暂不可用。",
                "原因：Milvus 向量数据库未启动，无法检索小说内容。",
                "请联系管理员启动 Milvus 服务后重试。",
                "[DONE]"
            );
        }

        // 1. 检索相关章节
        List<Document> docs = vectorStore.similaritySearch(question);
        if (docs.size() > topK) {
            docs = docs.subList(0, topK);
        }
        // TODO: bookId 过滤需要在检索后手动过滤
        if (bookId != null) {
            docs = docs.stream()
                .filter(doc -> bookId.toString().equals(doc.getMetadata().get("book_id")))
                .collect(Collectors.toList());
        }

        // 2. 构建上下文
        String context = docs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            return Flux.just("抱歉，未能找到与问题相关的小说内容。").concatWithValues("[DONE]");
        }

        // 3. 流式生成答案
        return chatClient.prompt()
            .system("你是一位小说阅读助手。请基于以下小说内容片段回答问题。" +
                "如果无法从片段中找到答案，请明确告知用户。回答要简洁准确。")
            .user("小说内容片段：\n" + context + "\n\n问题：" + question)
            .stream()
            .content()
            .concatWithValues("[DONE]");
    }

    @Override
    public RestResp<List<BookInfoRespDto>> similarBooks(Long bookId, Integer topK) {
        // Milvus 未启动时返回空列表
        if (vectorStore == null) {
            log.warn("[相似推荐降级] Milvus向量数据库未启用，返回空列表");
            return RestResp.ok(Collections.emptyList());
        }

        // 1. 获取该小说的向量表示
        // 由于 Milvus 不支持直接用 ID 查询向量，我们需要用小说简介作为查询文本
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            return RestResp.ok(Collections.emptyList());
        }

        String queryText = bookInfo.getBookName() + "。" + (bookInfo.getBookDesc() != null ? bookInfo.getBookDesc() : "");

        // 2. 语义检索
        List<Document> docs = vectorStore.similaritySearch(queryText);
        if (docs.size() > topK * 2) {
            docs = docs.subList(0, topK * 2);
        }

        // 3. 提取 book_id 并去重（排除自身）
        Set<Long> similarBookIds = docs.stream()
            .map(doc -> {
                Object id = doc.getMetadata().get("book_id");
                return id != null ? Long.valueOf(id.toString()) : null;
            })
            .filter(id -> id != null && !id.equals(bookId))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (similarBookIds.isEmpty()) {
            return RestResp.ok(Collections.emptyList());
        }

        // 4. 查询小说详情
        List<Long> ids = new ArrayList<>(similarBookIds);
        if (ids.size() > topK) {
            ids = ids.subList(0, topK);
        }

        List<BookInfo> books = bookInfoMapper.selectBatchIds(ids);
        List<BookInfoRespDto> result = books.stream().map(b -> BookInfoRespDto.builder()
            .id(b.getId())
            .bookName(b.getBookName())
            .categoryName(b.getCategoryName())
            .authorName(b.getAuthorName())
            .picUrl(b.getPicUrl())
            .bookDesc(b.getBookDesc())
            .wordCount(b.getWordCount())
            .lastChapterName(b.getLastChapterName())
            .build()).toList();

        return RestResp.ok(result);
    }
}
