package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.entity.BookChapter;
import io.github.xxyopen.novel.dao.entity.BookContent;
import io.github.xxyopen.novel.dao.mapper.BookChapterMapper;
import io.github.xxyopen.novel.dao.mapper.BookContentMapper;
import io.github.xxyopen.novel.service.BookVectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小说章节向量化服务实现
 * Milvus 未启动时自动跳过向量化操作
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Service
public class BookVectorServiceImpl implements BookVectorService {

    @Autowired(required = false)
    private VectorStore vectorStore;

    private final BookContentMapper bookContentMapper;
    private final BookChapterMapper bookChapterMapper;

    public BookVectorServiceImpl(BookContentMapper bookContentMapper, BookChapterMapper bookChapterMapper) {
        this.bookContentMapper = bookContentMapper;
        this.bookChapterMapper = bookChapterMapper;
    }

    /**
     * 章节内容切分大小（每块约 500 字）
     */
    private static final int CHUNK_SIZE = 500;

    /**
     * 切分重叠大小（防止语义断裂）
     */
    private static final int OVERLAP_SIZE = 50;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> indexChapter(Long bookId, Long chapterId, String content) {
        if (vectorStore == null) {
            log.debug("[向量索引跳过] Milvus未启用，跳过章节[{}]向量化", chapterId);
            return RestResp.ok();
        }

        if (content == null || content.isBlank()) {
            return RestResp.ok();
        }

        // 1. 先删除旧数据
        deleteChapterVector(chapterId);

        // 2. 文本切分
        List<String> chunks = splitText(content, CHUNK_SIZE, OVERLAP_SIZE);

        // 3. 构建 Document 列表（携带元数据）
        List<Document> documents = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            Document doc = new Document(
                chunks.get(i),
                Map.of(
                    "book_id", bookId.toString(),
                    "chapter_id", chapterId.toString(),
                    "chunk_index", String.valueOf(i),
                    "source", "novel_chapter"
                )
            );
            documents.add(doc);
        }

        // 4. 写入 Milvus（Spring AI 自动完成 Embedding）
        vectorStore.add(documents);
        log.info("章节 [{}] 已向量化入库，共 {} 块", chapterId, documents.size());

        return RestResp.ok();
    }

    @Override
    public RestResp<Void> indexBook(Long bookId) {
        if (vectorStore == null) {
            log.debug("[向量索引跳过] Milvus未启用，跳过小说[{}]全量索引", bookId);
            return RestResp.ok();
        }

        // 1. 删除旧索引
        deleteBookVectors(bookId);

        // 2. 查询小说所有章节
        LambdaQueryWrapper<BookChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookChapter::getBookId, bookId).orderByAsc(BookChapter::getChapterNum);
        List<BookChapter> chapters = bookChapterMapper.selectList(wrapper);

        for (BookChapter chapter : chapters) {
            BookContent content = bookContentMapper.selectOne(
                new LambdaQueryWrapper<BookContent>()
                    .eq(BookContent::getChapterId, chapter.getId())
            );
            if (content != null && content.getContent() != null) {
                indexChapter(bookId, chapter.getId(), content.getContent());
            }
        }

        log.info("小说 [{}] 全量索引完成，共 {} 章", bookId, chapters.size());
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteBookVectors(Long bookId) {
        if (vectorStore == null) {
            return RestResp.ok();
        }
        // 通过 filter 表达式删除该小说的所有向量
        // Milvus 支持通过表达式删除
        try {
            vectorStore.delete("book_id == '" + bookId + "'");
            log.info("小说 [{}] 的向量数据已删除", bookId);
        } catch (Exception e) {
            log.warn("删除小说 [{}] 向量数据失败: {}", bookId, e.getMessage());
        }
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteChapterVector(Long chapterId) {
        if (vectorStore == null) {
            return RestResp.ok();
        }
        try {
            vectorStore.delete("chapter_id == '" + chapterId + "'");
            log.info("章节 [{}] 的向量数据已删除", chapterId);
        } catch (Exception e) {
            log.warn("删除章节 [{}] 向量数据失败: {}", chapterId, e.getMessage());
        }
        return RestResp.ok();
    }

    /**
     * 滑动窗口文本切分
     */
    private List<String> splitText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            start += chunkSize - overlapSize;
        }
        return chunks;
    }
}
