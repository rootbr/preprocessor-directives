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

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после повторного start-raw-string - end-raw-string находится")
  public void test3() {
    final String line = "\" + @\"any text\" + @\"\" + @\"";

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после экранированные двойные кавычки - end-raw-string находится")
  public void test5() {
    final String line = "\"\"word\"\"\"\" is escaped\"\"\"";

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("end-raw-string не находится")
  public void test6() {
    final String line = "void goo() ";

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("в строке с экранированными кавычками не находится конец")
  public void test7() {
    final String line = "  \"\"documents\"\": {";

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("в строке с // находится конец")
  public void test8() {
    final String line = "} // end of class C\");";

    final var matcher = KeyPattern.RAW_STRING.end(line);

    assertThat(matcher.find()).isTrue();
  }
}
