package org.rootbr.preprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum KeyPattern {
  RAW_STRING(
      "@\".*$",
      "^.*" + Constants.ODD_NUMBER_OF_QUOTES
  ),
  COMMENT(
      "/\\*.*?$",
      "^.*\\*/"
  ),
  IF(
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#if[\\s\\t]+(!*?)(\\w+?)"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$",
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#endif"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$"
  ),
  ELSE_IF(
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#elif[\\s\\t]+(!*?)(\\w+?)"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$",
      null
  ),
  ELSE(
      "^"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + "#else"
          + Constants.ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER
          + Constants.ONE_LINE_COMMENT
          + Constants.LINES_COMMENT
          + "$",
      null
  );

  private final Pattern startPattern;
  private final Pattern endPattern;

  KeyPattern(String startPattern, String endPattern) {
    this.startPattern = Pattern.compile(startPattern);
    if (endPattern != null) {
      this.endPattern = Pattern.compile(endPattern);
    } else {
      this.endPattern = null;
    }
  }

  public Matcher start(String s) {
    final var stringWithoutComments = s
        .replaceAll(Constants.DELETE_STRING_IN_QUOTES, "")
        .replaceAll(Constants.DELETE_COMMENTS_IN_STRING, "")
        .replaceAll(Constants.DELETE_RAW_STRING_IN_STRING, "");
    return startPattern.matcher(stringWithoutComments);
  }

  public Matcher end(String s) {
    if (this.name().equals("RAW_STRING")) {
      return endPattern.matcher(s);
    } else {
      final var stringWithoutComments = s
          .replaceAll(Constants.DELETE_COMMENTS_IN_STRING, "")
          .replaceAll(Constants.DELETE_RAW_STRING_IN_STRING, "");
      return endPattern.matcher(stringWithoutComments);
    }
  }

  private static class Constants {
    static final String ODD_NUMBER_OF_QUOTES = "([^\"]|^)\"(\"\")*(?!\")";
    static final String ANY_NUMBER_WHITESPACE_OR_TABS_CHARACTER = "[\\s\\t]*";
    static final String ONE_LINE_COMMENT = "(//.*)*";
    static final String LINES_COMMENT = "(/\\*.*)*";
    static final String DELETE_COMMENTS_IN_STRING = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";
    static final String DELETE_RAW_STRING_IN_STRING = "(?:@\"(?:[^\"]|(?:\"{1}[^*/]))*\"{1})";
    static final String DELETE_STRING_IN_QUOTES = "((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/";
  }
}
