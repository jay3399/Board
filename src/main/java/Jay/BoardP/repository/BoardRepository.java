package Jay.BoardP.repository;


import static Jay.BoardP.domain.QBoard.board;
import static Jay.BoardP.domain.QFile.file;
import static org.springframework.util.StringUtils.hasText;

import Jay.BoardP.controller.dto.BoardSearch;
import Jay.BoardP.controller.dto.CategoryValue;
import Jay.BoardP.controller.dto.FileDto;
import Jay.BoardP.domain.Board;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

// springdatajpa 커스텀 -> 구현체이름 = a  //springdatajpa 인터페이스 이름 = a 맞춰주고 + Impl

// 커스텀에 몰아넣을까  or  분리할까 고민이 필요하다 .

// 조인으로 가져오는 페이징은 , 카운트쿼라를 분리해주는게 더 최적화에 좋다 나눌까말까 ? . 보통은 하는게 좋다

// 페이징 일대다 페치조인은 문제가있다.  하지만 작동은한다.. ?

// readonly true 기능을 사용하자 -> flush를 생략해서 성능향상을 이룰수있다 .

// cmd shift fn f12

// from.. from..
// sql은 단순히 , 필요한 데이터만 가져오는것을 목적으로하고 , 그것을 따로 애플리케이션 로직에서풀고 , 뷰는 프레젠테이션로직에서 풀어주는게 맞다 .
// 저 로직까지 sql에서 가져와서 복잡한 서브쿼리로 한방에처리하는것은 .. 좋지않다
// 디비는 단지 데이터만 그룹핑 , 필터링하여 가져오고 , 로직은 따로 가져야한다  - > 디비는 단순히 데이터만 퍼오는것에 집중한다

// 백단 .. 주로 어드민 생각보다 느려도 괜찮다 , sql 단에서 복잡하게 한방쿼리보다는 , 쿼리를 두번 세번 나눠서 호출하는게 괜찮을수도있다

// queryProjection -> 아키텍쳐적으로 고민이 필요하다 and 빌드를 추가적으로 해줘여한다 // 대안으로 필드나 컨스트럭터를 써도 괜찮다
//


//  QuerydslPredicateExecutor -> 편리한기능은 맞지만 실무상에서는 제약이있다 . ?

// -> 묵시적조인은 가능하지만 , left 조인이 불가능하다 .. ++쿼리dsl에 의존하게 된다


@Repository
//@RequiredArgsConstructor
public class BoardRepository {

    private final EntityManager em;

    private final SpringDataBoardRepository adBoardRepository;

    private final JPAQueryFactory jpaQueryFactory;

//    private final CheckCategory check;


    public BoardRepository(EntityManager em,
        SpringDataBoardRepository springDataBoardRepository, CheckCategory checkCategory) {
        this.em = em;
        this.adBoardRepository = springDataBoardRepository;
        this.jpaQueryFactory = new JPAQueryFactory(em);
//        this.check = checkCategory;

    }

    public Long saveBoardV2(Board board) {
        em.persist(board);
        return board.getId();
    }

    public Board findBoard(Long id) {

        return adBoardRepository.findById(id).get();
    }

    public Board findOne(Long id) {
        return em.find(Board.class, id);
    }


    public void deleteBoard(Long id) {
        adBoardRepository.deleteById(id);
    }


    public Page<Board> getWithoutSearch(Pageable pageable) {
        return adBoardRepository.findAllNotice(pageable);
    }

    public Page<Board> findAll(Pageable pageable) {
        return adBoardRepository.findAll(pageable);
    }

    public Page<Board> findAll(Pageable pageable, BoardSearch boardSearch , CheckCategory check) {

        // 카테고리코드 x , 검색키워드만

        String title = boardSearch.getTitle();
        String content = boardSearch.getContent();
        String complex = boardSearch.getComplex();
        String nickname = boardSearch.getNickname();


        BooleanBuilder builder = new BooleanBuilder();

        builder.and(board.isDeleted.eq(false));

        switch (check.validateValue(boardSearch)) {
            case "title" -> builder.and(getTitle(title));
            case "content" -> builder.and(getContent(content));
            case "nickname" -> builder.and(getNickname(nickname));
            case "complex" ->builder.and(getTitle(complex)).or(getContent(content));
        }

        return adBoardRepository.findAll(builder, pageable);

    }

    public Page<Board> findAll(Pageable pageable, BoardSearch boardSearch, String categoryCode , CheckCategory check) {


        String title = boardSearch.getTitle();
        String content = boardSearch.getContent();
        String complex = boardSearch.getComplex();
        String nickname = boardSearch.getNickname();


        BooleanBuilder builder = new BooleanBuilder();

        builder.and(board.isDeleted.eq(false));

        builder.and(getCategoryCode(categoryCode));

        switch (check.validateValue(boardSearch)) {
            case "title" -> builder.and(getTitle(title));
            case "content" -> builder.and(getContent(content));
            case "nickname"-> builder.and(getNickname(nickname));
            case "complex" -> builder.and(getTitle(complex).or(getContent(complex)));
        }

        return adBoardRepository.findAll(builder, pageable);



//
//        switch (check.validateValue(boardSearch)) {
//            case "title":
//                builder.and(getTitle(title));
//                break;
//            case "content":
//                builder.and(getContent(content));
//                break;
//            case "nickname":
//                builder.and(getNickname(nickname));
//                break;
//            case "complex":
//                builder.and(getTitle(complex).or(getContent(complex)));
//                break;
//        }



/**
 *  -------------------------------V2 ----------------------------------------------------------
 */

//        if (check.validate(categoryCode)) {
//            builder.and(getCategoryCode(categoryCode));
//        }
//
//        if (check.validate(categoryCode,title)){
//            builder.and(getCategoryCode(categoryCode)).and(getTitle(title));
//        }
//        else if (check.validate(title)) {
//            builder.and(getTitle(title));
//        }
//
//        if (check.validate(categoryCode , content)){
//            builder.and(getCategoryCode(categoryCode)).and(getContent(content));}
//        else if (check.validate(content)) {
//            builder.and(getContent(content));
//        }
//
//        if (check.validate(categoryCode , nickname)){
//            builder.and(getCategoryCode(categoryCode)).and(getNickname(nickname));}
//        else if (check.validate(nickname)) {
//            builder.and(getNickname(nickname));
//        }
//
//        if (check.validate(categoryCode , complex)){
//            builder.and(getCategoryCode(categoryCode)).and(getTitle(complex).or(getContent(complex)));}
//        else if (check.validate(complex)) {
//            builder.and(getTitle(complex).or(getContent(complex)));
//        }
//
//


/**
 *  -------------------------------V1 ----------------------------------------------------------
 */
//        if (hasText(categoryCode)) {
//            builder.and(getCategoryCode(categoryCode));
//        }
//
//        if (hasText(categoryCode) && hasText(title)) {
//            builder.and(getCategoryCode(categoryCode)).and(getTitle(title));
//        } else if (hasText(title)) {
//            builder.and(getTitle(title));
//        }
//
//        if (hasText(categoryCode) && hasText(content)) {
//            builder.and(getCategoryCode(categoryCode)).and(getContent(content));
//        } else if (hasText(content)) {
//            builder.and(getContent(content));
//        }
//
//        if (hasText(categoryCode) && hasText(nickname)) {
//            builder.and(getCategoryCode(categoryCode)).and(getNickname(nickname));
//        } else if (hasText(nickname)) {
//            builder.and(getNickname(nickname));
//        }
//
//        if (hasText(categoryCode) && hasText(complex)) {
//            builder.and(getCategoryCode(categoryCode)).and(getTitle(complex).or(getContent(complex)));
//        } else if (hasText(complex)) {
//            builder.and(getTitle(complex).or(getContent(complex)));
//        }

    }

    private static BooleanExpression getCategoryCode(String categoryCode) {
        return board.category.code.eq(categoryCode);
    }
    private static BooleanExpression getNickname(String nickname) {
        return board.createBy.eq(nickname);
    }

    private static BooleanExpression getContent(String content) {
        return board.content.like("%" + content + "%");
    }

    private static BooleanExpression getTitle(String title) {
        return board.title.like("%" + title + "%");
    }



    public Page<Board> findByConditionV2(Pageable pageable, String condition) {

        return adBoardRepository.findAllByCategory_Code(pageable, condition);
    }


    public List<Board> myBoardList(Long memberId) {
        return adBoardRepository.findAllByMember_Id(memberId);
    }

    public Page<Board> findAllBySort(Pageable pageable) {
        return adBoardRepository.findAll(pageable);
    }

    public Long getView(Long boarId) {
        return jpaQueryFactory.select(board.countVisit).from(board).where(board.id.eq(boarId))
            .fetchOne();
    }

    public void addViewCountWithRedis(Long boarId, Long viewCount) {
        jpaQueryFactory.update(board).set(board.countVisit, viewCount)
            .where(board.id.eq(boarId))
            .execute();
    }


    public List<FileDto> getFiles(Long boarId) {

        return jpaQueryFactory.select(
                Projections.constructor(FileDto.class, file.originalFilename, file.storedFilename,
                    file.fileType)).from(file)
            .where(file.board.id.eq(boarId)).fetch();

    }


}




