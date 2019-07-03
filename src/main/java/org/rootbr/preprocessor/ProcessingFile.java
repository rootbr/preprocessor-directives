package org.rootbr.preprocessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingFile {
  private static final Logger log = LoggerFactory.getLogger(ProcessingFile.class);

  private final Properties properties;

  private static final Pattern patternIf =
      Pattern.compile("^[\\s\\t]*#if[\\s\\t]+(!*?)(\\w+?)[\\s\\t]*(//.*)*(/\\*.*)*$");
  private static final Pattern patternElseIf =
      Pattern.compile("^[\\s\\t]*#elif[\\s\\t]+(!*?)(\\w+?)[\\s\\t]*(//.*)*(/\\*.*)*$");
  private static final Pattern patternElse =
      Pattern.compile("^[\\s\\t]*#else[\\s\\t]*(//.*)*(/\\*.*)*$");
  private static final Pattern patternEnd =
      Pattern.compile("^[\\s\\t]*#endif[\\s\\t]*(//.*)*(/\\*.*)*$");

  public ProcessingFile(Properties properties) {
    this.properties = properties;
  }


  public void processingFile(final Path f, final Path t, Charset charset) {
    try {
      final var lines = Files.readAllLines(f, charset);
      executeDirective(lines, f.toString());
      try (BufferedWriter writer = Files.newBufferedWriter(t, charset, StandardOpenOption.CREATE)) {
        Files.write(t, lines, charset);
      }
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }

  public void executeDirective(List<String> lines, String f) {
    var matchIf = false;
    var hasTrueCondition = false;
    var needWrite = true;

    final var iterator = lines.iterator();
    while (iterator.hasNext()) {
      final var s = iterator.next();
      if (!matchIf) {
        Matcher matcherIf = patternIf.matcher(s);
        if (matcherIf.find()) {
          iterator.remove();
          matchIf = true;
          needWrite = is(matcherIf.group(2), matcherIf.group(1).length());
          hasTrueCondition = needWrite;
        }
      } else {
        if (hasTrueCondition) {
          Matcher matcherElseIf = patternElseIf.matcher(s);
          if (patternEnd.matcher(s).matches()) {
            iterator.remove();
            matchIf = false;
            hasTrueCondition = false;
            needWrite = true;
          } else if (patternElse.matcher(s).matches()) {
            iterator.remove();
            needWrite = !needWrite;
          } else if (matcherElseIf.find()) {
            iterator.remove();
            needWrite = false;
          } else if (!needWrite) {
            iterator.remove();
          }
        } else {
          Matcher matcherElseIf = patternElseIf.matcher(s);
          if (patternEnd.matcher(s).matches()) {
            iterator.remove();
            matchIf = false;
            hasTrueCondition = false;
            needWrite = true;
          } else if (patternElse.matcher(s).matches()) {
            iterator.remove();
            needWrite = !needWrite;
          } else if (matcherElseIf.find()) {
            iterator.remove();
            needWrite = !needWrite && is(matcherElseIf.group(2), matcherElseIf.group(1).length());
            hasTrueCondition = needWrite;
          } else if (!needWrite) {
            iterator.remove();
          }
        }

      }
    }
    if (matchIf) {
      log.warn("{} Directive #if does not have #endif!", f);
    }
  }

  private boolean is(String key, int numberOfNegation) {
    var b = Boolean.parseBoolean(properties.getProperty(key));
    while (numberOfNegation-- > 0) {
      b = !b;
    }
    return b;
  }
}
