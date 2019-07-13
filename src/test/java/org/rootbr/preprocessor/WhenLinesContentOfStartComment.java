package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WhenLinesContentOfStartComment {
  @Test
  public void test0() {
    final String comment = "/*" + KeyWordsPattern.IF.keyWord() + " true";

    final var matcher = KeyWordsPattern.COMMENT_START.matcher(comment);

    assertThat(matcher.find()).isFalse();
  }
}
