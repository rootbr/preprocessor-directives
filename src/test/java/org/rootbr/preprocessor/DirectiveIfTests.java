package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rootbr.preprocessor.engine.KeyPattern;

public class DirectiveIfTests {
  @Test
  @DisplayName("директива #if после комментария /* не находится")
  public void test0() {
    final String comment = "/* #if true";

    final var matcher = KeyPattern.IF.start(comment);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("директива #if после комментария // не находится")
  public void test1() {
    final String comment = "// #if true";

    final var matcher = KeyPattern.IF.start(comment);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("директива #if перед комментарием // находится")
  public void test2() {
    final String comment = "#if true //";

    final var matcher = KeyPattern.IF.start(comment);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("директива #if перед комментарием /* не находится, т. к. по спецификации невозможно")
  public void test3() {
    final String comment = "#if true /*";

    final var matcher = KeyPattern.IF.start(comment);

    assertThat(matcher.find()).isFalse();
  }
}
