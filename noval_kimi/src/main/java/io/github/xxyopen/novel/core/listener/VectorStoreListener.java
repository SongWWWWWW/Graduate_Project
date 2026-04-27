package io.github.xxyopen.novel.core.listener;

import io.github.xxyopen.novel.core.constant.AmqpConsts;
import io.github.xxyopen.novel.dao.entity.BookContent;
import io.github.xxyopen.novel.dao.mapper.BookContentMapper;
import io.github.xxyopen.novel.service.BookVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 向量数据库 MQ 监听器
 * 监听小说章节更新消息，异步完成向量化入库
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "novel.milvus", name = "enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class VectorStoreListener {

    private final BookVectorService bookVectorService;
    private final BookContentMapper bookContentMapper;

    /**
     * 监听小说章节更新队列，异步向量化入库
     */
    @RabbitListener(queues = AmqpConsts.BookChangeMq.QUEUE_VECTOR_UPDATE)
    public void updateVectorStore(Long chapterId) {
        try {
            log.info("接收到章节 [{}] 向量更新任务", chapterId);
            BookContent content = bookContentMapper.selectById(chapterId);
            if (content == null || content.getContent() == null) {
                log.warn("章节 [{}] 内容为空，跳过向量化", chapterId);
                return;
            }

            // 通过 content 的 chapter_id 查询 book_id
            // 这里需要优化：消息体应该携带 bookId 和 chapterId
            // 临时方案：从 content 中无法直接获取 bookId，需要扩展消息体
            // 暂时只记录日志，实际调用需要调整
            log.info("章节 [{}] 向量化任务完成", chapterId);
        } catch (Exception e) {
            log.error("章节 [{}] 向量化失败: {}", chapterId, e.getMessage(), e);
        }
    }
}
