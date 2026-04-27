package io.github.xxyopen.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 作者稿费收入响应DTO
 */
@Data
@Builder
public class AuthorIncomeRespDto {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "小说ID")
    private Long bookId;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "收入月份")
    @JsonFormat(pattern = "yyyy-MM")
    private LocalDate incomeMonth;

    @Schema(description = "税前收入;单位：元")
    private String preTaxIncome;

    @Schema(description = "税后收入;单位：元")
    private String afterTaxIncome;

    @Schema(description = "支付状态;0-待支付 1-已支付")
    private Integer payStatus;

    @Schema(description = "稿费确认状态;0-待确认 1-已确认")
    private Integer confirmStatus;

    @Schema(description = "详情")
    private String detail;
}
