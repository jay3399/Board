package Jay.BoardP.repository;

import Jay.BoardP.controller.dto.BoardSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CheckCategoryImpl implements CheckCategory {


  @Override
  public boolean validate(String categoryCode) {

    if (StringUtils.hasText(categoryCode)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean validate(String categoryCode, String value) {

    if (StringUtils.hasText(categoryCode) && StringUtils.hasText(value)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean validate(String categoryCode, BoardSearch boardSearch) {

    if (!StringUtils.hasText(categoryCode) && boardSearch == null) {
      return false;
    }
    return true;
  }

  @Override
  public String validateValue(BoardSearch boardSearch) {

    String value = "";

    if (StringUtils.hasText(boardSearch.getTitle())) {
       value = "title";
    }

    if (StringUtils.hasText(boardSearch.getContent())) {
       value = "content";
    }
    if (StringUtils.hasText(boardSearch.getNickname())) {
       value = "nickname";
    }

    if (StringUtils.hasText(boardSearch.getComplex())) {
       value = "complex";
    }

    return value;

  }

}
