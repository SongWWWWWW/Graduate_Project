package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminCommentRespDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private Long chapterId;
    private String chapterName;
    private String commentContent;
    private Integer auditStatus;
    private String createTime;
}
