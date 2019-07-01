package org.rootbr.preprocessor;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class DefinedSymbolsTests {

  @Test
  public void getBooleanProperty() throws IOException {
    final DefinedSymbols definedSymbols = new DefinedSymbols("tests/defined-symbols.properties");

    final var prop = definedSymbols.prop("FEATURE_MANAGED_ETW");

    assertThat(prop).isTrue();
  }
}
