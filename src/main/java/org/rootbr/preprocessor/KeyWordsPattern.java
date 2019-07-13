package org.rootbr.preprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum KeyWordsPattern {
  RAW_STRING_START(
      "@\"",
      "^.*"
          + "@\""
          + Constants.ODD_NUMBER_OF_QUOTES
          + "$"
  ),
  RAW_STRING_END(
      "\"",
      "^.*"
          + Constants.ODD_NUMBER_OF_QUOTES
          + ".*$"
  ),
  COMMENT_START(
      "\"*",
      ".*@\"/*"
  ),
  COMMENT_END(
      "*\"",
      "/*\""
  ),
  IF(
      "#if",
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#if[\\s\\t]+(!*?)(\\w+?)"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$"
  ),
  ELSE_IF(
      "#elif",
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#elif[\\s\\t]+(!*?)(\\w+?)"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$"
  ),
  ELSE(
      "#else",
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#else"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$"
  ),
  ENDIF(
      "#endif",
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#endif"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$"
  );


  private final String keyWord;
  private final Pattern pattern;

  KeyWordsPattern(String keyWord, String pattern) {
    this.keyWord = keyWord;
    this.pattern = Pattern.compile(pattern);
  }

  public String keyWord() {
    return keyWord;
  }

  public Matcher matcher(String s) {
    return pattern.matcher(s);
  }

  private static class Constants {
    static final String ODD_NUMBER_OF_QUOTES = "[^\"]*(?:[^\"]*\"[^\"]*\")*[^\"]*";
    static final String ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER = "[\\s\\t]*";
    static final String ONE_LINE_COMMENT = "(//.*)*";
    static final String LINES_COMMENT = "(/\\*.*)*";
  }
}
