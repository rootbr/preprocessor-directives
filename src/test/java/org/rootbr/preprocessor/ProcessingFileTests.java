package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rootbr.preprocessor.BuilderLines.BODY;
import static org.rootbr.preprocessor.BuilderLines.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rootbr.preprocessor.engine.workers.DirectiveProcessingAndCopyWorker;

class ProcessingFileTests {
  static DirectiveProcessingAndCopyWorker worker;

  @BeforeAll
  static void setUp() throws IOException {
    Properties properties = new Properties();
    properties.put("PROPERTY_TRUE", "true");
    properties.put("PROPERTY_FALSE", "false");
    properties.put("PROPERTY_ANY", "any");
    worker = new DirectiveProcessingAndCopyWorker(properties);
  }

  @Test
  @DisplayName("когда передаем на обработку пустой список, тогда выводится пустой список")
  public void test0() {
    List<String> lines = new ArrayList<>();

    worker.processingDirective(lines);

    assertThat(lines).isEmpty();
  }

  @Test
  @DisplayName("строки с директивами #if, #elif, #else, #endif не выводятся")
  public void test1() {
    List<String> lines = builder().trueIf().trueElif().else_().endIf();

    worker.processingDirective(lines);

    assertThat(lines).isEmpty();
  }

  @Nested
  @DisplayName("когда передаем директиву #if с неверным условием")
  class Tests1{
    BuilderLines falseIf;

    @BeforeEach
    public void setUp() {
      falseIf = builder().falseIf();
    }

    @Test
    @DisplayName("тело if не выводится")
    public void test2() {
      List<String> lines = falseIf.withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }

    @Test
    @DisplayName("тело истиного #elif выводится")
    public void test3() {
      List<String> lines = falseIf.trueElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).containsExactly(BODY);
    }

    @Test
    @DisplayName("тело второго истиного #elif (предыдущий #elif ложный) выводится")
    public void test4() {
      List<String> lines = falseIf.falseElif().trueElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).containsExactly(BODY);
    }

    @Test
    @DisplayName("тело ложного #elif не выводится")
    public void test5() {
      List<String> lines = falseIf.falseElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }

    @Test
    @DisplayName("тело #else выводится")
    public void test6() {
      List<String> lines = falseIf.else_().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).containsExactly(BODY);
    }
  }

  @Nested
  @DisplayName("когда передаем директиву #if с верным уловием")
  class Tests2 {
    BuilderLines trueIf;

    @BeforeEach
    public void setUp() {
      trueIf = builder().trueIf();
    }

    @Test
    @DisplayName("тело if выводится")
    public void test0() {
      List<String> lines = trueIf.withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).containsExactly(BODY);
    }

    @Test
    @DisplayName("тело истиного #elif не выводится")
    public void test1() {
      List<String> lines = trueIf.trueElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }

    @Test
    @DisplayName("тело второго истиного #elif не выводится")
    public void test2() {
      List<String> lines = trueIf.trueElif().trueElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }

    @Test
    @DisplayName("тело ложного #elif не выводится")
    public void test3() {
      List<String> lines = trueIf.falseElif().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }

    @Test
    @DisplayName("тело #else не выводится")
    public void test4() {
      List<String> lines = trueIf.else_().withBody().endIf();

      worker.processingDirective(lines);

      assertThat(lines).doesNotContain(BODY);
    }
  }
}
