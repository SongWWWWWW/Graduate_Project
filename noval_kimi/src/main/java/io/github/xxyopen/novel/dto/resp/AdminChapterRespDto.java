package io.github.xxyopen.novel.dto.resp;
import lombok.Data;
@Data
public class AdminChapterRespDto {
    private Long id;
    private String chapterName;
    private Integer chapterNum;
    private Integer wordCount;
    private Integer isVip;
    private String updateTime;
}
