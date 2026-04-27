package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminBookRespDto {
    private Long id;
    private String bookName;
    private String picUrl;
    private Long authorId;
    private String authorName;
    private String categoryName;
    private Integer wordCount;
    private Long visitCount;
    private Float score;
    private Integer bookStatus;
    private String createTime;
}
