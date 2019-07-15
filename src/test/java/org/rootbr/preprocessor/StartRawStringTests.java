package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rootbr.preprocessor.engine.KeyPattern;

@DisplayName("Когда начинается необработанная строка")
public class StartRawStringTests {
  @Test
  @DisplayName("start-raw-string находится")
  public void test2() {
    final String line = "var test = @\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после end-raw-string и снова start-raw-string - start-raw-string находится")
  public void test3() {
    final String line = "@\"any text\" + @\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после end-raw-string - start-raw-string не находится")
  public void test4() {
    final String line = "@\"any text\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("после end-raw-string - start-raw-string не находится")
  public void test40() {
    final String line = "private const string LocalRegistryPath = @\"Roslyn\\Internal\\Analyzers\\AB\\Vsix\\\";";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("после экранированных двойных кавычек - start-raw-string находится")
  public void test5() {
    final String line = "@\"this \"\"word\"\"\"\" is escaped";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после комментария // - start-raw-string не находится")
  public void test7() {
    final String line = "//var a = \"/\" + @\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("после комментария /* - start-raw-string находится (начало комментариев должно обрабатываться первее)")
  public void test8() {
    final String line = "var a = \"/\" /* + @\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("после завершенного комментария /* */ - start-raw-string находится")
  public void test9() {
    final String line = "var a = \"/\" + /* comment */ @\"";

    final var matcher = KeyPattern.RAW_STRING.start(line);

    assertThat(matcher.find()).isTrue();
  }
}
