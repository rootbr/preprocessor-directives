package org.rootbr.preprocessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

public class DefinedSymbols {
  private final Properties properties = new Properties();

  public DefinedSymbols(final String path) throws IOException {
    try (InputStream input = new FileInputStream(path)) {
      properties.load(new InputStreamReader(input, Charset.forName("UTF-8")));
    }
  }

  public Boolean prop(final String propertyName) {
    return Boolean.parseBoolean(properties.getProperty(propertyName));
  }
}
