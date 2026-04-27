package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminLoginRespDto {
    private String token;
    private Long userId;
    private String username;
    private String roleName;
}
