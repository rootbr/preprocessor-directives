package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Когда начинается необработанная строка")
public class StartRawStringTests {
  @Test
  @DisplayName("начало raw-string находится")
  public void test2() {
    final String line = "var test = @\"";

    final var matcher = KeyWordsPattern.RAW_STRING_START.matcher(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("начало raw-string находится даже после raw-string")
  public void test3() {
    final String line = "@\"any text\" + @\"";

    final var matcher = KeyWordsPattern.RAW_STRING_START.matcher(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("когда raw-string в одну строку, тогда начало raw-string не находится")
  public void test4() {
    final String line = "@\"any text\"";

    final var matcher = KeyWordsPattern.RAW_STRING_START.matcher(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("когда в строке начинается raw-string и идёт экранированные двойные кавычки, тогда начало raw-string находится")
  public void test5() {
    final String line = "@\"this \"\"word\"\" is escaped";

    final var matcher = KeyWordsPattern.RAW_STRING_START.matcher(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("когда в строке начинается raw-string и идёт несколько экранированных двойных кавычек, тогда начало raw-string находится")
  public void test6() {
    final String line = "@\"any text\"\"any text\"\"";

    final var matcher = KeyWordsPattern.RAW_STRING_START.matcher(line);

    assertThat(matcher.find()).isTrue();
  }
}
