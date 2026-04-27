package io.github.xxyopen.novel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 内容审核日志
 * </p>
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@TableName("audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String bizType;

    private Long bizId;

    private Byte contentType;

    private String content;

    private String localResult;

    private String localHits;

    private String cloudResult;

    private String cloudDetail;

    private String finalResult;

    private Byte status;

    private Long operatorId;

    private String operatorRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public Byte getContentType() { return contentType; }
    public void setContentType(Byte contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLocalResult() { return localResult; }
    public void setLocalResult(String localResult) { this.localResult = localResult; }
    public String getLocalHits() { return localHits; }
    public void setLocalHits(String localHits) { this.localHits = localHits; }
    public String getCloudResult() { return cloudResult; }
    public void setCloudResult(String cloudResult) { this.cloudResult = cloudResult; }
    public String getCloudDetail() { return cloudDetail; }
    public void setCloudDetail(String cloudDetail) { this.cloudDetail = cloudDetail; }
    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorRemark() { return operatorRemark; }
    public void setOperatorRemark(String operatorRemark) { this.operatorRemark = operatorRemark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
