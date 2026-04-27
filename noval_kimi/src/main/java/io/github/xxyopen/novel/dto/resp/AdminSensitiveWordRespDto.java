package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminSensitiveWordRespDto {
    private Long id;
    private String word;
    private String category;
    private Integer enabled;
    private String createTime;
}
