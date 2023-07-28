package Jay.BoardP.controller.dto;

import Jay.BoardP.domain.Board;
import Jay.BoardP.domain.FileType;
import Jay.BoardP.domain.Member;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPostDto {

    private String nickName;
    @NotBlank
    private String title;
    @NotBlank
    private String content;

    private String ipAddress;

    private String categoryCode;

    private Long userId;


    private Map<FileType, List<MultipartFile>> attachmentFiles = new ConcurrentHashMap<>();

    @Builder
    public BoardPostDto(String nickName, String title, String content,
        Map<FileType, List<MultipartFile>> attachmentFiles , String ipAddress , String categoryCode , Long userId) {
        this.nickName = nickName;
        this.title = title;
        this.attachmentFiles = attachmentFiles;
        this.ipAddress = ipAddress;
        this.content = content;
        this.categoryCode = categoryCode;
        this.userId = userId;
    }


    public Board createBoard(Member member) {
        return Board.createBoard(title, content, ipAddress, member, categoryCode);
    }



}

