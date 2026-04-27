package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminAiSessionRespDto {
    private String sessionId;
    private Long userId;
    private String title;
    private String mode;
    private String createTime;
}
