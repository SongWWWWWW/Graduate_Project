package io.github.xxyopen.novel.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务配置属性
 * 支持多 Provider 切换（SiliconFlow / Kimi / OpenAI 等）
 */
@Data
@Component
@ConfigurationProperties(prefix = "novel.ai")
public class AiProperties {

    /** 提供商: siliconflow | kimi | openai */
    private String provider = "kimi";

    /** API Key */
    private String apiKey;

    /** Base URL，如 https://api.moonshot.cn/v1 */
    private String baseUrl;

    /** 模型名称，如 moonshot-v1-8k */
    private String model;

    /** 默认温度 */
    private Double temperature = 0.7;

    /** 最大 token 数 */
    private Integer maxTokens = 4096;

    /** 是否启用 LLM 内容审核 */
    private Boolean auditEnabled = true;
}
