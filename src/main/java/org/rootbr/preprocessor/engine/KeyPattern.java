package org.rootbr.preprocessor.engine;

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
      "^" + Constants.SEPARATOR + "*"
          + "#if"
          + Constants.SEPARATOR + "+"
          + "(!*?)(\\w+?)"
          + Constants.SEPARATOR + "*" + Constants.ONE_LINE_COMMENT + "$",
      "^" + Constants.SEPARATOR + "*"
          + "#endif"
          + Constants.SEPARATOR + "*" + Constants.ONE_LINE_COMMENT + "$"
  ),
  ELSE_IF(
      "^" + Constants.SEPARATOR + "*"
          + "#elif"
          + Constants.SEPARATOR + "+"
          + "(!*?)(\\w+?)"
          + Constants.SEPARATOR + "*" + Constants.ONE_LINE_COMMENT + "$",
      null
  ),
  ELSE(
      "^"
          + Constants.SEPARATOR + "*"
          + "#else"
          + Constants.SEPARATOR + "*" + Constants.ONE_LINE_COMMENT + "$",
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
    static final String SEPARATOR = "[\\s]";
    static final String ODD_NUMBER_OF_QUOTES = "([^\"]|^)\"(\"\")*(?!\")";
    static final String ONE_LINE_COMMENT = "(//.*)*";
    static final String DELETE_COMMENTS_IN_STRING = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";
    static final String DELETE_RAW_STRING_IN_STRING = "(?:@\"(?:[^\"]|(?:\"{1}[^*/]))*\"{1})";
    static final String DELETE_STRING_IN_QUOTES =
        "((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|//[^\\n]*|/\\*(?:[^*]|\\*(?!/))*\\*/";
  }
}
