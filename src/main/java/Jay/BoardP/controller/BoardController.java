package Jay.BoardP.controller;

import static Jay.BoardP.controller.Attributes.*;
import static Jay.BoardP.controller.RedisAttributes.*;

import Jay.BoardP.controller.form.BoardAddForm;
import Jay.BoardP.controller.dto.BoardCommentDto;
import Jay.BoardP.controller.dto.BoardDetailedDto;
import Jay.BoardP.controller.dto.BoardListDto;
import Jay.BoardP.controller.dto.BoardPostDto;
import Jay.BoardP.controller.dto.BoardSearch;
import Jay.BoardP.controller.dto.CategoryValue;
import Jay.BoardP.controller.dto.CommentDto;
import Jay.BoardP.controller.dto.FileDto;
import Jay.BoardP.controller.dto.Role;
import Jay.BoardP.controller.dto.User;
import Jay.BoardP.controller.form.CommentForm;
import Jay.BoardP.controller.form.PenaltyForm;
import Jay.BoardP.controller.form.BoardEditForm;
import Jay.BoardP.domain.Board;
import Jay.BoardP.domain.Member;
import Jay.BoardP.service.BoardService;
import Jay.BoardP.service.CommentService;
import Jay.BoardP.service.PenaltyService;
import Jay.BoardP.service.RedisService;
import Jay.BoardP.service.memberService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final PenaltyService penaltyService;

    private final RedisService redisService;

    private final RedisTemplate redisTemplate;

    private final memberService memberService;

    @GetMapping("/boards")
    public String boardMainPage(Model model,
        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.DESC)
        Pageable pageable) {

        Page<BoardListDto> pages = boardService.getPages(pageable);

        int nowPage = pages.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 9, pages.getTotalPages());

        model.addAttribute("pageList", pages);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board/boardList";
    }

    @GetMapping("/boards/{categoryCode}/category")
    public String BoardList(
        @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
        @PathVariable("categoryCode") String categoryCode, Model model,
        @ModelAttribute("boardSearch") BoardSearch boardSearch) {

        Page<BoardListDto> boardListV2;

        if (categoryCode.equals("NON")) {
            if (searchExist(boardSearch)) boardListV2 = boardService.getBoardWithoutCategory(boardSearch, pageable);
            else boardListV2 = boardService.getBoardWithoutCategory(pageable);
        } else boardListV2 = boardService.getBoardWithCategory(boardSearch, pageable, categoryCode);


        model.addAttribute("pageList", boardListV2);
        model.addAttribute("categoryV", categoryCode);
        model.addAttribute("boardSearch", boardSearch);

        return "board/boardList";
    }



    @GetMapping("/boards/{boardId}")
    public String getBoardV3WithRedisCount(@PathVariable("boardId") Long boardId, Model model,
        @ModelAttribute("commentForm")
        CommentForm commentForm,HttpServletRequest req, @AuthenticationPrincipal User user) {

        if (deleteStatus(boardId)) {
            return "redirect:/boards/ALL/category";
        }

        String ipAddress = getIpAddress(req);

        String key = BOARDCOUNTPERDAY;

        //3분마다 , 스케줄이용 viewCnt 값을 디비에 반영
        if (redisService.isFirstRequest(ipAddress, boardId)) {
            boardService.viewCountWithRedisWithFirst(boardId);   // viewCnt
            redisService.writeRequest(ipAddress, boardId);   // 중복방지
            makeUpdateCount(key); //일일 전체 보드 카운트수
        }

        // 첫요청x
        boardService.viewCountWithRedis(boardId);

        List<BoardCommentDto> commentDto = commentService.findByBoardIdV2(boardId);

        String viewCnt = String.valueOf(redisTemplate.opsForValue().get("viewCnt::" + boardId));

        BoardDetailedDto boardDetailedDto = boardService.findBoardOnCount(boardId);

        List<FileDto> boardsFiles = boardService.findBoardsFiles(boardId);

        String nickname = user.getNickname();

        model.addAttribute(NICKNAME, nickname);
        model.addAttribute(BOARD, boardDetailedDto);
        model.addAttribute(COMMENTS, commentDto);
        model.addAttribute(FILES, boardsFiles);
        model.addAttribute(VIEWCNT, viewCnt);

        return "board/boardDeatailed";

    }


    private Boolean deleteStatus(Long boardId) {
        return boardService.deleteValidated(boardId);
    }


    @GetMapping("/boards/post")
    public String boardForm(@ModelAttribute("boardForm") BoardAddForm boardAddForm,
        @AuthenticationPrincipal User user , HttpServletRequest request) {

        boardAddForm.setInfo(user, getIpAddress(request));

        //boardAddForm.setNickname(user.getNickname());
//        boardAddForm.setUserId(user.getId());
//        boardAddForm.setIpAddress(getIpAddress(request));


        return "board/boardForm";
    }


    @PostMapping("/boards/post")
    public String addBoard(@Validated @ModelAttribute("boardForm") BoardAddForm boardAddForm,
        BindingResult bindingResult, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            log.info("bindingResult :{}", bindingResult);
            return "board/boardForm";
        }

        // 권한 인증 , 오직 어드민만 공지사항작성가능
        // 프론트단에서 막아놔도 , 스크립트상으로 접근가능 -> 컨트롤러에서 한번더 검증
        if (isNotice(boardAddForm, user)) {
            return "redirect:/boards/post";
        }

        // 컨트롤러단과 , 서비스의 결합을 최대한 느슨하게해야한다
        // 컨트롤러의 파라미터를 그대로 서비스단으로 넘기면 form , 즉 컨트롤러단에 의지를 하는것 .
        // 특히 서비스단은 최대한 순수하게 유지를 해줘야한다  , 의존 최대한 x
        // 파라미터 개수또한 최대한 제한한다 , 유지보수 관점

        BoardPostDto boardPostDto = boardAddForm.createBoardPostDto();

        Long boardId = boardService.addBoardV3(boardPostDto);

        makeUpdateCount(BOARDPERDAY);

        redirectAttributes.addAttribute(BOARDID, boardId);

        return "redirect:/boards/{boardId}";

    }

    private void makeUpdateCount(String key) {
        if (!redisTemplate.hasKey(key)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, 0L);
            valueOperations.increment(key);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    private static boolean isNotice(BoardAddForm boardAddForm, User user) {
        return boardAddForm.getCategoryCode().equals("NOTICE") && user.getRole() == Role.USER;
    }

    @GetMapping("/boards/{boardId}/report")
    private String reportBoardForm(@ModelAttribute("penalty") PenaltyForm penaltyForm) {
        return "board/boardReportForm";
    }


    @ResponseBody
    @PostMapping("/boards/{boardId}/report")
    private Boolean reportBoard(@PathVariable Long boardId, @AuthenticationPrincipal User user,
        @ModelAttribute
        PenaltyForm penaltyForm) {
        return penaltyService.exist(user.getId(), boardId, penaltyForm);
    }


    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoardV2(@PathVariable Long boardId, @AuthenticationPrincipal User user) {

        Member one = memberService.findOne(user.getId());
        Board board = getBoardValidator(boardId, one.getBoardList());

        if (board == null) {
            return "redirect:/boards/ALL/category";
        }

        boardService.deleteBoardV2(board);
        return "redirect:/boards/ALL/category";
    }

    private static Board getBoardValidator(Long boardId, List<Board> boardList) {
        return boardList.stream().filter(board1 -> board1.getId().equals(boardId)).findFirst()
            .orElseGet(null);
    }

    @GetMapping("/boards/{boardId}/edit")
    public String editBoard(@AuthenticationPrincipal User user, @PathVariable Long boardId,
        Model model, RedirectAttributes redirectAttributes) {

        Board board = boardService.findBoard(boardId);
        Member one = memberService.findOne(user.getId());

        List<Board> boardList = one.getBoardList();

        Board board2 = boardList.stream().filter(
            board1 -> board1.getCreateBy().equals(board.getCreateBy())
        ).findAny().get();

        redirectAttributes.addAttribute(BOARDID, boardId);

        if (board2 == null) {
            return "redirect:/boards/{boardId}";
        }

        BoardDetailedDto board1 = boardService.findBoardOnCount(boardId);

        BoardEditForm boardEditForm = BoardEditForm.createBoardEditForm(board1);

        model.addAttribute(BOARD, boardEditForm);

        return "board/boardEditForm";


    }


    @PostMapping("/boards/{boardId}/edit")
    public String editBoard(@Validated @ModelAttribute("board") BoardEditForm boardEditForm,
        BindingResult bindingResult, @PathVariable Long boardId, @AuthenticationPrincipal User user,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);
            return "board/boardEditForm";
        }

        boardService.editBoard(boardId, boardEditForm);
        redirectAttributes.addAttribute(BOARDID, boardId);

        return "redirect:/boards/{boardId}";

    }


    //    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId, @AuthenticationPrincipal User user) {
        boardService.deleteBoard(boardId, user.getId());
        return "redirect:/boards";
    }


    @ModelAttribute("categoryValues")
    private List<CategoryValue> categoryValues() {
        List<CategoryValue> categoryValues = new ArrayList<>();
        categoryValues.add(new CategoryValue("FREE", "잡담"));
        categoryValues.add(new CategoryValue("QNA", "질문"));
        categoryValues.add(new CategoryValue("FUN", "유머"));

        return categoryValues;
    }


    @ModelAttribute("reportValues")
    private List<CategoryValue> reportValues() {
        List<CategoryValue> reportValues = new ArrayList<>();
        reportValues.add(new CategoryValue("F", "욕설"));
        reportValues.add(new CategoryValue("R", "규정위반"));

        return reportValues;
    }


    private static boolean searchExist(BoardSearch boardSearch) {
        return StringUtils.hasText(boardSearch.getTitle()) || StringUtils.hasText(
            boardSearch.getContent()) || StringUtils.hasText(boardSearch.getNickname())
            || StringUtils.hasText(boardSearch.getComplex());
    }

    private static String getIpAddress(HttpServletRequest req) {

        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = req.getHeader("WL-Proxy-Client-IP"); // 웹로직
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = req.getRemoteAddr();
        }

        return ip;
    }

}
