package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class DirectiveIfTests {
  @Test
  public void test0() {
    final String comment = "/*#if true";

    final var matcher = KeyWordsPattern.IF.matcher(comment);

    assertThat(matcher.find()).isFalse();
  }
}
