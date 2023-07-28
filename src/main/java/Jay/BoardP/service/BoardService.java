package Jay.BoardP.service;
import Jay.BoardP.controller.dto.AdBoardListDto;
import Jay.BoardP.controller.dto.BoardDetailedDto;
import Jay.BoardP.controller.dto.BoardListDto;
import Jay.BoardP.controller.dto.BoardPostDto;
import Jay.BoardP.controller.dto.BoardSearch;
import Jay.BoardP.controller.dto.FileDto;
import Jay.BoardP.controller.dto.MyBoardListDto;
import Jay.BoardP.controller.form.BoardEditForm;
import Jay.BoardP.domain.Board;
import Jay.BoardP.domain.Category;
import Jay.BoardP.domain.File;
import Jay.BoardP.domain.Member;
import Jay.BoardP.repository.BoardRepository;
import Jay.BoardP.repository.CheckCategory;
import Jay.BoardP.repository.CheckCategoryImpl;
import Jay.BoardP.repository.MemberRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;

    private final FileService fileService;

    private final RedisTemplate redisTemplate;

    private final MemberRepository memberRepository;

    private final CheckCategory checkCategory;


    @Transactional
    public Long addBoardV2(Long memberId,BoardPostDto boardPostDto) throws IOException {

        Member member = memberRepository.findMember(memberId);

        Board board = boardPostDto.createBoard(member);


        List<File> files = fileService.saveFiles(boardPostDto.getAttachmentFiles() ,board);

        for (File file : files) {
            log.info(file.getOriginalFilename());
        }

        return boardRepository.saveBoardV2(board);
    }

    @Transactional
    public Long addBoardV3(BoardPostDto boardPostDto) throws IOException {

        System.out.println("boardPostDto = " + boardPostDto.getUserId());

        Member member = memberRepository.findMember(boardPostDto.getUserId());

        Board board = boardPostDto.createBoard(member);

        List<File> files = fileService.saveFiles(boardPostDto.getAttachmentFiles() ,board);

        for (File file : files) {
            log.info(file.getOriginalFilename());
        }

        return boardRepository.saveBoardV2(board);
    }

    public Board findBoard(Long boardId) {
        return boardRepository.findBoard(boardId);
    }


    public Board findOne(Long boardId) {
        return boardRepository.findOne(boardId);
    }

    @Cacheable(value = "board"  , cacheManager = "cacheManagerV3" , key = "#boardId")
    public BoardDetailedDto findBoardOnCount(Long boardId) {

        Board board = boardRepository.findBoard(boardId);

        return BoardDetailedDto.from(board);
    }

    @Cacheable(value = "file"  , cacheManager = "cacheManagerV3" , key = "#boardId")
    public List<FileDto> findBoardsFiles(Long boardId) {
        return boardRepository.getFiles(boardId);
    }


    public void deleteBoard(Long boardId, Long memberId) {

        Board one = boardRepository.findBoard(boardId);
        Member member = one.getMember();

        if (member.getId() == memberId) {
            one.setIsDeleted(true);
        }
    }


    @Transactional
    public void deleteBoardV2(Board board) {
        board.setIsDeleted(true);
    }


    public Boolean deleteValidated(Long boardId) {
        Board board = boardRepository.findOne(boardId);

        if (board.getIsDeleted()) {
            return true;
        }

        return false;
    }



    public Page<BoardListDto> getBoardWithCategory(BoardSearch boardSearch, Pageable pageable, String categoryCode) {

        CheckCategory checkCategory = new CheckCategoryImpl();

        Page<Board> allV4 = boardRepository.findAll(pageable, boardSearch, categoryCode,
            checkCategory);

        return allV4.map(BoardListDto::from);
    }

    //

    public Page<BoardListDto> getBoardWithoutCategory(BoardSearch boardSearch, Pageable pageable) {
        Page<Board> allV3 = boardRepository.findAll(pageable, boardSearch, checkCategory);
        return allV3.map(BoardListDto::from);

    }

    public Page<BoardListDto> getBoardWithoutCategory(Pageable pageable) {
        Page<Board> withoutSearch = boardRepository.getWithoutSearch(pageable);
        return withoutSearch.map(BoardListDto::from);
    }


    public Page<BoardListDto> getPages(Pageable pageable) {

        Page<Board> boards = boardRepository.findAll(pageable);
        return boards.map(BoardListDto::from);
    }
//
    public Page<BoardListDto> getPagesByCondition(Pageable pageable,String condition) {
        Page<Board> results = boardRepository.findByConditionV2(pageable, condition);
        return results.map(BoardListDto::from
        );
    }

    public Page<AdBoardListDto> getAdminBoardSort(Pageable pageable) {

        Page<Board> allBySort = boardRepository.findAllBySort(pageable);

        return allBySort.map(
            board -> AdBoardListDto.createAdBoardList(board)
        );
    }

    public List<MyBoardListDto> getMyBoardList(Long memberId) {
        List<Board> boards = boardRepository.myBoardList(memberId);

        return boards.stream().map(
            board -> MyBoardListDto.createMyBoardList(board)
        ).collect(Collectors.toList());

    }

    @Transactional
    public void editBoard(Long boardId , BoardEditForm boardEditForm) {
        Board board = boardRepository.findBoard(boardId);
        board.setTitle(boardEditForm.getTitle());
        board.setContent(boardEditForm.getContent());
        board.setCategory(Category.createCategory(boardEditForm.getCategoryCode()));
    }

    public void viewCountWithRedis(Long boardId) {

        String key = "viewCnt::" + boardId;
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //3분 지났을시
        if (valueOperations.get(key) == null) {
            valueOperations.set(
                key,
                boardRepository.getView(boardId),
                Duration.ofMinutes(5)
            );
        }


    }

    public void viewCountWithRedisWithFirst(Long boardId) {
        String key = "viewCnt::" + boardId;
        ValueOperations valueOperations = redisTemplate.opsForValue();


        //삭제됐을시에 .  3분지난시점
        if (valueOperations.get(key) == null) {
            valueOperations.set(
                key,
                boardRepository.getView(boardId),
                Duration.ofMinutes(5)
            );

            valueOperations.increment(key);
            // 삭제안됌 , 그대로 키가져와서 value 값 up !!!!
        } else{
            valueOperations.increment(key);
        }

    }

}
