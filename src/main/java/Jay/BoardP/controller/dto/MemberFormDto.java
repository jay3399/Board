package Jay.BoardP.controller.dto;


import Jay.BoardP.domain.Member;
import java.sql.Date;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class MemberFormDto {

    private Long id;

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^(?!\\d+$)\\w{4,8}$", message = "아이디는 특수문자를제외한 영문,또는 영문+숫자 4~8자리여야 합니다.")
    private String userId;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~6자리여야 합니다.")
    private String nickname;
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String checkPassword;

    @Email(message = "올바른 이메일형식을 입력해주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요")
    private String code;

    @NotBlank
    private String phone;

    private Date birthDate;


    public void setMemberForm(Member member) {
        this.userId = member.getUserId();
        this.nickname = member.getNickname();
        this.password = member.getPassword();
        this.email = member.getEmail();
        this.phone = member.getPhoneNumber();
        this.birthDate = member.getBirthDate();
    }


    public static MemberFormDto createMemberFromEntity(Member member) {
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setMemberForm(member);
        return memberFormDto;
    }

    public Member createMember(String password) {
        return Member.builder().phoneNumber(phone).userId(userId).email(email)
            .birthDate(birthDate).nickname(nickname).password(password).build();
    }

}
