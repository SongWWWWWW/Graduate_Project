package io.github.xxyopen.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作者资料更新请求DTO
 */
@Data
@Schema(description = "作者资料更新请求")
public class AuthorProfileUpdateReqDto {

    @NotBlank(message = "笔名不能为空")
    @Schema(description = "笔名", required = true)
    private String penName;

    @Schema(description = "手机号码")
    private String telPhone;

    @Schema(description = "QQ或微信账号")
    private String chatAccount;

    @Schema(description = "电子邮箱")
    private String email;

    @NotNull(message = "作品方向不能为空")
    @Schema(description = "作品方向;0-男频 1-女频", required = true)
    private Integer workDirection;
}
