package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Когда заканчивается необработанная строка")
public class EndRawStringTests {
  @Test
  @DisplayName("end-raw-string находится")
  public void test2() {
    final String line = "\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после повторного start-raw-string - end-raw-string находится")
  public void test3() {
    final String line = "\" + @\"any text\" + @\"\" + @\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }


  @Test
  @DisplayName("после экранированные двойные кавычки - end-raw-string находится")
  public void test5() {
    final String line = "\"\"word\"\"\"\" is escaped\"\"\"";

    final var matcher = KeyWordsPattern.RAW_STRING_END.matcher(line);

    assertThat(matcher.find()).isTrue();
  }
}
