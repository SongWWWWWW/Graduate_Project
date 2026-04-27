package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminUserRespDto {
    private Long id;
    private String username;
    private String nickName;
    private Integer status;
    private String createTime;
}
