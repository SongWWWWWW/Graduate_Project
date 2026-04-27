package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminSysLogRespDto {
    private Long id;
    private String operation;
    private String method;
    private String params;
    private Long duration;
    private String createTime;
}
