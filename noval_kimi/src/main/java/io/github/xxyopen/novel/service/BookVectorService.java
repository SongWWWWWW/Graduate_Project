package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;

/**
 * 小说章节向量化服务接口
 * 负责将小说章节内容切分、Embedding 后写入 Milvus
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
public interface BookVectorService {

    /**
     * 将单章小说内容向量化并入库
     *
     * @param bookId    小说ID
     * @param chapterId 章节ID
     * @param content   章节内容
     * @return 操作结果
     */
    RestResp<Void> indexChapter(Long bookId, Long chapterId, String content);

    /**
     * 批量向量化小说的所有章节（全量重建索引）
     *
     * @param bookId 小说ID
     * @return 操作结果
     */
    RestResp<Void> indexBook(Long bookId);

    /**
     * 删除小说的所有向量数据
     *
     * @param bookId 小说ID
     * @return 操作结果
     */
    RestResp<Void> deleteBookVectors(Long bookId);

    /**
     * 删除单章向量数据
     *
     * @param chapterId 章节ID
     * @return 操作结果
     */
    RestResp<Void> deleteChapterVector(Long chapterId);
}
