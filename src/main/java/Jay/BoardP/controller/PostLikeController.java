package Jay.BoardP.controller;


import Jay.BoardP.controller.dto.User;
import Jay.BoardP.service.PostLikeService;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;


    @PostMapping("/{boardId}/postLike")
    @ResponseBody
    public Boolean postLike(@PathVariable Long boardId, @AuthenticationPrincipal User user,
        RedirectAttributes redirectAttributes, HttpServletResponse response) {
        Long memberId = user.getId();
        return postLikeService.pushLikeButton(boardId, memberId);
    }

}
