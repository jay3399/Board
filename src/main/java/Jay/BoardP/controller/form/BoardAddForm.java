package Jay.BoardP.controller.form;

import Jay.BoardP.controller.dto.BoardPostDto;
import Jay.BoardP.controller.dto.User;
import Jay.BoardP.domain.FileType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
public class BoardAddForm {


    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 20 , message = "20자이상 입력해주세요")
    private String content;

    private List<MultipartFile> imageFiles;
    private List<MultipartFile> generalFiles;

    @NotBlank(message = "카테고리가 비어있습니다")
    private String categoryCode;


    private String nickname;
    private String ipAddress;
    private Long userId;

//
//    @Builder
//    private BoardAddForm(String title, String content, List<MultipartFile> imageFiles,
//        List<MultipartFile> generalFiles , String categoryCode , String nickname , String ipAddress , String userId) {
//        this.title = title;
//        this.content = content;
//        this.imageFiles = (imageFiles != null) ? imageFiles : new ArrayList<>();
//        this.generalFiles = (generalFiles != null) ? generalFiles : new ArrayList<>();
//        this.categoryCode = categoryCode;
//        this.nickname = nickname;
//        this.ipAddress = ipAddress;
//        this.userId = Long.valueOf(userId);
//    }

    public BoardPostDto createBoardPostDto() {
        Map<FileType, List<MultipartFile>> files = getAttachmentTypeListMap();
        return BoardPostDto.builder()
            .nickName(nickname)
            .title(title)
            .content(content)
            .ipAddress(ipAddress)
            .categoryCode(categoryCode)
            .userId(userId)
            .attachmentFiles(files)
            .build();
    }


    private Map<FileType, List<MultipartFile>> getAttachmentTypeListMap() {
        Map<FileType, List<MultipartFile>> attachments = new ConcurrentHashMap<>();
        attachments.put(FileType.IMAGE, imageFiles);
        attachments.put(FileType.GENERAL, generalFiles);
        return attachments;
    }

    public void setInfo(User user, String ipAddress) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.ipAddress = ipAddress;
    }



}
