package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminAuditLogRespDto {
    private Long id;
    private String bizType;
    private Long bizId;
    private String authorName;
    private String content;
    private String localResult;
    private String localHits;
    private String finalResult;
    private Integer status;
    private Long operatorId;
    private String createTime;
}
