package io.github.xxyopen.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者资料响应DTO
 */
@Data
@Builder
public class AuthorProfileRespDto {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "笔名")
    private String penName;

    @Schema(description = "手机号码")
    private String telPhone;

    @Schema(description = "QQ或微信账号")
    private String chatAccount;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "作品方向;0-男频 1-女频")
    private Integer workDirection;

    @Schema(description = "作家状态;0-正常")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
