package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rootbr.preprocessor.BuilderLines.BODY;
import static org.rootbr.preprocessor.BuilderLines.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProcessingFileTests {
  static ProcessingFile processing;

  @BeforeAll
  static void setUp() throws IOException {
    Properties properties = new Properties();
    properties.put("PROPERTY_TRUE", "true");
    properties.put("PROPERTY_FALSE", "false");
    properties.put("PROPERTY_ANY", "any");
    processing = new ProcessingFile(properties);
  }

  @Test
  @DisplayName("когда передаем на обработку пустой список, тогда выводится пустой список")
  public void test0() {
    List<String> lines = new ArrayList<>();

    processing.executeDirective(lines, null);

    assertThat(lines).isEmpty();
  }

  @Test
  @DisplayName("строки с директивами #if, #elif, #else, #endif не выводятся")
  public void test1() {
    List<String> lines = builder().trueIf().trueElif().else_().endIf();

    processing.executeDirective(lines, null);

    assertThat(lines).isEmpty();
  }

  @Nested
  @DisplayName("когда передаем директиву #if с неверным уловием")
  class Tests1{

    @Test
    @DisplayName("тело if не выводится")
    public void test2() {
      List<String> lines = builder().falseIf().withBody().endIf();

      processing.executeDirective(lines, null);

      assertThat(lines).isEmpty();
    }

    @Test
    @DisplayName("тело #else выводится")
    public void test3() {
      List<String> lines = builder().falseIf().else_().withBody().endIf();

      processing.executeDirective(lines, null);

      assertThat(lines).containsExactly(BODY);
    }
  }

  @Nested
  @DisplayName("когда передаем директиву #if с верным уловием")
  class Tests2 {
    @Test
    @DisplayName("тело if выводится")
    public void test4() {
      List<String> lines = builder().trueIf().withBody().endIf();

      processing.executeDirective(lines, null);

      assertThat(lines).containsExactly(BODY);
    }

    @Test
    @DisplayName("тело #else не выводится")
    public void test3() {
      List<String> lines = builder().trueIf().else_().withBody().endIf();

      processing.executeDirective(lines, null);

      assertThat(lines).isEmpty();
    }
  }
}
