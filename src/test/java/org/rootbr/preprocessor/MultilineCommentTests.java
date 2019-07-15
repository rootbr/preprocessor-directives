package org.rootbr.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rootbr.preprocessor.engine.KeyPattern;

public class MultilineCommentTests {
  @Test
  @DisplayName("начало комментария находится /*")
  public void test0() {
    final String line = "/* comment */ /* new comment";

    final var matcher = KeyPattern.COMMENT.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("начало комментария не находится для законеченного комментария /* */")
  public void test1() {
    final String line = "/* comment */";

    final var matcher = KeyPattern.COMMENT.start(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("конец комментария находится")
  public void test2() {
    final String line = "*/ var g = 1";

    final var matcher = KeyPattern.COMMENT.end(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("удалить из строки все до конца комментария")
  public void test3() {
    final String line = "comment */ var g = 1";
    final var matcher = KeyPattern.COMMENT.end(line);

    final var b = matcher.find();

    assertThat(b).isTrue();
    assertThat(matcher.replaceAll("")).isEqualTo(" var g = 1");
  }

  @Test
  @DisplayName("начало комментария не находится в строке")
  public void test4() {
    final String line = "Region(\"span\", \"/* Hello ...\", autoCollapse: true));";

    final var matcher = KeyPattern.COMMENT.start(line);

    assertThat(matcher.find()).isFalse();
  }

  @Test
  @DisplayName("начало комментария находится в строке с //")
  public void test5() {
    final String line = "Region(\"span\", \"// Hello ...\", autoCollapse: true));/*";

    final var matcher = KeyPattern.COMMENT.start(line);

    assertThat(matcher.find()).isTrue();
  }

  @Test
  @DisplayName("комментарий в строке не находится")
  public void test6() {
    final String line = "                Diagnostic(ErrorCode.WRN_DebugFullNameTooLong, \"Main\").WithArguments(\"AVeryLong TSystem.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Collections.Generic.List`1[[System.Int32, mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089\").WithLocation(12, 20));";

    final var matcher = KeyPattern.COMMENT.start(line);

    assertThat(matcher.find()).isFalse();
  }
}
