package io.github.xxyopen.novel.core.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 * 显式创建 VectorStore bean，支持 Milvus Standalone / Milvus Lite
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "novel.milvus", name = "enabled", havingValue = "true")
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.milvus.client.host:localhost}")
    private String milvusHost;

    @Value("${spring.ai.vectorstore.milvus.client.port:19530}")
    private int milvusPort;

    @Value("${spring.ai.vectorstore.milvus.collection.name:novel_chapters}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.collection.embedding-dimension:1024}")
    private int embeddingDimension;

    /**
     * 创建 Milvus 客户端
     */
    @Bean(destroyMethod = "close")
    public MilvusServiceClient milvusClient() {
        log.info("初始化 Milvus 客户端: {}:{}", milvusHost, milvusPort);
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusHost)
                .withPort(milvusPort)
                .build();
        return new MilvusServiceClient(connectParam);
    }

    /**
     * 创建 Milvus VectorStore
     * 使用 Spring AI 的 MilvusVectorStore builder 模式
     */
    @Bean
    public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        log.info("初始化 Milvus VectorStore: collection={}, metric=IP", collectionName);
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(collectionName)
                .metricType(MetricType.IP)
                .initializeSchema(false)
                .build();
    }
}
