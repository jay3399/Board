package Jay.BoardP.controller;
import static Jay.BoardP.controller.RedisAttributes.*;
import static org.springframework.util.StringUtils.hasText;
import Jay.BoardP.controller.dto.MemberFormDto;
import Jay.BoardP.controller.dto.User;
import Jay.BoardP.domain.Member;
import Jay.BoardP.service.EmailService;
import Jay.BoardP.service.memberService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final memberService memberService;
    private final EmailService emailService;
    private final RedisTemplate redisTemplate;


    @GetMapping("/signUp")
    public String addForm(@ModelAttribute("memberFormDto") MemberFormDto memberFormDto
    ) {
        return "members/createMemberform";
    }


    @PostMapping("/signUp")
    public String addMember(@Validated@ModelAttribute("memberFormDto") MemberFormDto memberFormDto,
        BindingResult bindingResult) {


//        String code = String.valueOf(redisTemplate.opsForValue().get(memberFormDto.getEmail()));

        String password = memberFormDto.getPassword();
        String checkPassword = memberFormDto.getCheckPassword();
        String nickname = memberFormDto.getNickname();

        System.out.println("nickname = " + nickname);
        System.out.println("checkPassword = " + checkPassword);
        System.out.println("password = " + password);


        //비밀번호 재확인 검증
        if (!memberFormDto.getPassword().equals(memberFormDto.getCheckPassword())) {
            bindingResult.reject("checkPassword", "비밀번호가 일치하지 않습니다");
        }

        //이메일코드 인증
//        if (code == null || !code.equals(memberFormDto.getCode())) {
//            bindingResult.reject("checkCode", "이메일 인증번호가 일치하지 않습니다");
//        }

        if (bindingResult.hasErrors()) {
            System.out.println("bindingResult = " + bindingResult);
            return "members/createMemberForm";
        }

        memberService.save(memberFormDto);

        makeUpdateCount();
        redisTemplate.delete(memberFormDto.getEmail());

        return "redirect:/";
    }

    @GetMapping("/release")
    public String releaseHumanForm() {
        return "members/validateMember";
    }

    @PostMapping("/release")
    public String validateHuman(@RequestParam String password,
        RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {

        if (isValidatedHuman(user.getId(), password) || !hasText(password)) {
            redirectAttributes.addAttribute("mismatch", "비밀번호가 일치하지않습니다");
            return "redirect:/members/{id}/validate";
        }
        memberService.releaseHuman(user.getId());

        return "redirect:/login";
    }

    private boolean isValidatedHuman(Long id, String password) {
        return !memberService.findOne(id).getPassword().equals(password);
    }


    @PostMapping("/signUp/mail")
    @ResponseBody
    public boolean Mail(String email) {

        boolean isDup = false;

        Member byEmail = memberService.findByEmail(email);

        // 이메일 및 , 인증번호 중복방지
        if (byEmail == null && !redisTemplate.hasKey(email)) {
            emailService.mailCheck(email);
        } else {
            isDup = true;
        }

        return isDup;
    }

    @PostMapping("/signUp/mail/check")
    @ResponseBody
    public boolean codeCheck(String code, HttpServletRequest request) {

        boolean isCheck = false;

        HttpSession session = request.getSession();

        if (code.equals(session.getAttribute("email"))) {
            isCheck = true;
        }

        return isCheck;
    }


    @PostMapping("/signUp/duplicateCheck")
    @ResponseBody
    public Boolean duplicateCheck(@RequestParam String userId) {
        Boolean isDuplicated = false;

        if (memberService.findByUserId(userId) != null) {
            isDuplicated = true;
        }
        return isDuplicated;
    }


    @PostMapping("/add/duplicateCheckByEmail")
    public Boolean duplicateCheckByEmail(@RequestParam String email) {
        Boolean isDuplicated = false;

        if (memberService.findByEmail(email) != null) {
            isDuplicated = true;
        }
        return isDuplicated;
    }

    private void makeUpdateCount() {
        if (!redisTemplate.hasKey(SIGNUPPERDAY)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(SIGNUPPERDAY, 0L);
            valueOperations.increment(SIGNUPPERDAY);
        } else {
            redisTemplate.opsForValue().increment(SIGNUPPERDAY);
        }


    }
}
