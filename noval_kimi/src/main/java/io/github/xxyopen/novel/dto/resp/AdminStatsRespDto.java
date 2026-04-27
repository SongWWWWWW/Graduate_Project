package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminStatsRespDto {
    private Long userCount;
    private Long authorCount;
    private Long bookCount;
    private Long commentCount;
    private Long todayNewUsers;
    private Long todayNewBooks;
}
