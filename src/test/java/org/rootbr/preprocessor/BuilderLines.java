package org.rootbr.preprocessor;

import java.util.ArrayList;
import java.util.List;

public class BuilderLines {
  public static final String BODY = "body";
  final List<String> lines = new ArrayList<>();

  public static BuilderLines builder() {
    return new BuilderLines();
  }

  public List<String> endIf() {
    lines.add("#endif");
    return lines;
  }

  public BuilderLines trueIf() {
    lines.add("#if PROPERTY_TRUE");
    return this;
  }

  public BuilderLines startMultilineComment() {
    lines.add("/*");
    return this;
  }

  public BuilderLines endMultilineCommentWithIf() {
    lines.add("*/ #if PROPERTY_FALSE");
    return this;
  }

  public BuilderLines falseIf() {
    lines.add("#if PROPERTY_FALSE");
    return this;
  }

  public BuilderLines withBody() {
    lines.add(BODY);
    return this;
  }

  public BuilderLines else_() {
    lines.add("#else");
    return this;
  }

  public BuilderLines trueElif() {
    lines.add("#elif PROPERTY_TRUE");
    return this;
  }

  public BuilderLines falseElif() {
    lines.add("#elif PROPERTY_FALSE");
    return this;
  }
}
