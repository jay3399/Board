# Community
커뮤니티

## 개인프로젝트
- 소개
    * 개인프로젝트입니다.
    * 심플한 게시판을 갖고있는 커뮤니티이지만 ,이전보다 조금 더 성능적개선을 고려한 게시판입니다.
- 목적
    * 간단한 게시판이지만 , 성능적인부분을 최대한 고려한 게시판을 만들고싶었습니다
    * ORM기술인 jpa에대해 공부해보고 싶었으며 이전의 공부방식인 책이나 강의를보고 프로젝트를 경험해보는 방식이아닌 , 이번에는 직접 프로젝트로 먼저 부딛혀보고 그 이후 부족한 부분에대해 매꿔가는 방식으로 배워가며 그 과정속에서 마주한 문제를 해결하고 부족한 성능을 개선해보는 과정을 담아보고 싶었습니다.
- 문제,해결
    * 주로 jpa 매핑과 성능문제를 많이 맞닥뜨렸으며 책과 강의를통해 문제를 해결하려 했습니다. 그리고 jpa성능에대한 문제를 해결하다보니 ,전반적인 성능개선에대해서도 관심을 갖게되었고 redis와 scheduler , batch에대한 필요성을 느끼게되어 적용해가며 어느정도 배워볼수잇는 기회였습니다.
- 후기
    * 프로젝트를 진행해가며 ,jpa뿐만이아닌 다양한 기술들을 경험해볼수있었고 이런저런 오류를 해결하고 구조를 바꿔가며 유지보수성이 좋은 코드에대한 필요성또한 느꼈습니다. 그렇기에 앞으로는 특정기술에대한    학습보다는 ,객체지향과 조금더어떻게하면 깔끔한 코드를 작성할수있을지에대해 고민을 더 해보려합니다 .

## 개발환경
- IntelliJ , MacOS
- java11
- SpringBoot 2.7.6 (Gradle)
- SpringSecurity  , SpringBatch
- Jpa , QueryDSL
- MySQL, Redis
- ThymeLeaf
- .

## 기능
- 사용자
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/LoginController.java">로그인</a>
        + 스프링시큐리티이용 로그인
        + 로그인 회원의 권한에 맞게 ADMIN/USER/HUMAN 구분해서 화면표시
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/MemberController.java">회원가입</a>
        + 이메일인증 비동기처리, 레디스 인증키관리
        + Validated 이용 컨트롤러단 검증
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/BoardController.java">게시판</a>
        + 게시글작성, 조회(*레디스 캐시적용) ,수정 ,삭제
        + 게시글 좋아요 및 신고
        + 게시글카테고리별 정렬, 검색 및 페이징 , 대표이미지,  공지사항 상단노출
        + 조회수카운트및 최적화 및  중복방지 ( * 레디스&스케줄러 적용 )
        + 경고회원은 게시글작성및 댓글작성 불가능
        + <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/CommentController.java">댓글 및 대댓글 작성 및 삭제</a>
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/MyPageController.java">마이페이지</a>
        + 본인게시글 및 댓글조회
        + 회원탈퇴
        + 비밀번호변경 x
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/HomeController.java">방문자수</a>
        + 레디스이용 , 일일방문자 및 총방문자표시

- 관리자
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/controller/AdminController.java">회원및게시판관리</a>
        + 게시판페이징 댓글및 조회수,좋아요별 정렬
        + 삭제된 게시글 표시및정리

- 시스템
    * <a href="https://github.com/jay3399/Board/blob/master/src/main/java/Jay/BoardP/BatchScheduler.java">일일데이터및 월별데이터 처리</a>
        + 배치+스케줄러이용 매일 특정시간 신고누적게시글딜리트처리/총방문자수업데이트/일일방문자수&게시글수&가입자수&조회수반영
        + 매월 특정시간 오래된게시글및 휴먼회원처리/일별데이터조회후월별데이터업데이트 
