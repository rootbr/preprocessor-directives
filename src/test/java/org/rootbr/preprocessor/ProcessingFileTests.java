package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcessingFileTests {
  static ProcessingFile processing;

  @BeforeAll
  static void setUp() throws IOException {
    processing = new ProcessingFile("tests/defined-symbols.properties");
  }

  @Test
  @DisplayName("когда передаем на обработку пустой список, то вернётся пустой список")
  public void test0() {
    List<String> lines = new ArrayList<>();

    processing.executeDirective(lines, null);

    assertThat(lines).isEmpty();
  }

  @Test
  @DisplayName("строка с директивой #if не попадает в конечный список")
  public void test1() {
    List<String> lines = new ArrayList<>();
    lines.add("#if !FEATURE_MANAGED_ETW");
    lines.add("#endif");

    processing.executeDirective(lines, null);

    assertThat(lines).doesNotContain("#if !FEATURE_MANAGED_ETW");
  }

  @Test
  @DisplayName("когда передаем директиву #if с неверным уловием, содержание if не выводится")
  public void test2() {
    List<String> lines = new ArrayList<>();
    lines.add("#if !FEATURE_MANAGED_ETW");
    lines.add("строки не должно быть");
    lines.add("#endif");

    processing.executeDirective(lines, null);

    assertThat(lines).isEmpty();
  }
}
