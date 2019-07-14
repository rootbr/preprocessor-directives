package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DirectiveIfTests {
  @Test
  @DisplayName("директива #if после комментария /* не находится")
  public void test0() {
    final String comment = "/* #if true";

    final var matcher = KeyWordsPattern.IF.matcher(comment);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("директива #if после комментария // не находится")
  public void test1() {
    final String comment = "// #if true";

    final var matcher = KeyWordsPattern.IF.matcher(comment);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("директива #if перед комментарием // находится")
  public void test2() {
    final String comment = "#if true //";

    final var matcher = KeyWordsPattern.IF.matcher(comment);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("директива #if перед комментарием /* находится")
  public void test3() {
    final String comment = "#if true /*";

    final var matcher = KeyWordsPattern.IF.matcher(comment);

    assertThat(matcher.find()).isTrue();
  }
}
