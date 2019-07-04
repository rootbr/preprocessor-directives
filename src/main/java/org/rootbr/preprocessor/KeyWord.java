package org.rootbr.preprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum KeyWord {
  RAW_STRING_START("@\"", null),
  RAW_STRING_END("\"", null),
  COMMENT_START("\"*", null),
  COMMENT_END("*\"", null),
  IF("#if", Pattern.compile("^[\\s\\t]*#if[\\s\\t]+(!*?)(\\w+?)[\\s\\t]*(//.*)*(/\\*.*)*$")),
  ELSE_IF("#elif", Pattern.compile("^[\\s\\t]*#elif[\\s\\t]+(!*?)(\\w+?)[\\s\\t]*(//.*)*(/\\*.*)*$")),
  ELSE("#else", Pattern.compile("^[\\s\\t]*#else[\\s\\t]*(//.*)*(/\\*.*)*$")),
  ENDIF("#endif", Pattern.compile("^[\\s\\t]*#endif[\\s\\t]*(//.*)*(/\\*.*)*$"));

  private final String keyWord;
  private final Pattern pattern;

  KeyWord(String keyWord, Pattern pattern) {
    this.keyWord = keyWord;
    this.pattern = pattern;
  }

  public String keyWord() {
    return keyWord;
  }

  public Matcher matcher(String s) {
    return pattern.matcher(s);
  }
}
