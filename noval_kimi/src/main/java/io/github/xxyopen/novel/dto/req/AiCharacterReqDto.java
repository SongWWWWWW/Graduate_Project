package io.github.xxyopen.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 角色设定请求 DTO
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Data
@Schema(description = "AI角色设定请求")
public class AiCharacterReqDto {

    @Schema(description = "设定ID（新增时为空）")
    private Long id;

    @Schema(description = "关联小说ID")
    private Long bookId;

    @NotBlank(message = "设定名不能为空")
    @Schema(description = "设定名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "角色人设/世界观设定")
    private String persona;

    @Schema(description = "文风描述")
    private String style;

    @Schema(description = "是否默认设定;0-否 1-是")
    private Integer isDefault;
}
