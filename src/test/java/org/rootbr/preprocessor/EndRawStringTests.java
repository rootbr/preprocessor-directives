package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Когда заканчивается необработанная строка")
public class EndRawStringTests {
  @Test
  @DisplayName("конец raw-string находится")
  public void test2() {
    final String line = "\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("конец raw-string находится даже в строке c raw-string")
  public void test3() {
    final String line = "\" + @\"any text\" @\"\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }


  @Test
  @DisplayName("когда в строке raw-string идёт экранированные двойные кавычки, тогда конец raw-string находится")
  public void test5() {
    final String line = "\"\"word\"\" is escaped\"\"\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }
}
