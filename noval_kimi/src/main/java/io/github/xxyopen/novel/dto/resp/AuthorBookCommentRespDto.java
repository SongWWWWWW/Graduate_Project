package io.github.xxyopen.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者作品评论响应DTO
 */
@Data
@Builder
public class AuthorBookCommentRespDto {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "小说ID")
    private Long bookId;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "小说封面")
    private String bookPic;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String userNickName;

    @Schema(description = "评论用户头像")
    private String userPhoto;

    @Schema(description = "评论内容")
    private String commentContent;

    @Schema(description = "回复数量")
    private Integer replyCount;

    @Schema(description = "审核状态;0-待审核 1-审核通过 2-审核不通过")
    private Integer auditStatus;

    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
