package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 快速创建作者响应 DTO
 */
@Data
public class AdminAuthorCreateRespDto {

    @Schema(description = "作者ID")
    private Long id;

    @Schema(description = "笔名")
    private String penName;
}
