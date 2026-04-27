package io.github.xxyopen.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 作者稿费收入明细响应DTO
 */
@Data
@Builder
public class AuthorIncomeDetailRespDto {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "小说ID")
    private Long bookId;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "收入日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate incomeDate;

    @Schema(description = "订阅总额;单位：元")
    private String incomeAccount;

    @Schema(description = "订阅次数")
    private Integer incomeCount;

    @Schema(description = "订阅人数")
    private Integer incomeNumber;
}
