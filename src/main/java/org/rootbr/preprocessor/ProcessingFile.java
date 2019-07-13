package org.rootbr.preprocessor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingFile {
  private static final Logger log = LoggerFactory.getLogger(ProcessingFile.class);

  private final Properties properties;

  public ProcessingFile(Properties properties) {
    this.properties = properties;
  }


  public void processingCSharpFile(final Path f, final Path t, Charset charset) {
    try {
      final var lines = Files.readAllLines(f, charset);
      final var s = executeDirective(lines);
      if (s != null) {
        log.warn("file {} is wrong ({})", f.toString(), s);
      }
      Files.write(t, lines, charset, StandardOpenOption.CREATE);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }

  public String executeDirective(List<String> lines) {
    var matchIf = false;
    var hasTrueCondition = false;
    var needWrite = true;

    final var iterator = lines.iterator();
    //TODO перейти на стек/очередь чтобы учитывать предыдущие важные структуры, например, начатый комментарий или сырой текст
    Deque<KeyWord> deque = new ArrayDeque<>();
    while (iterator.hasNext()) {
      final var s = iterator.next();
      if (!matchIf) {
        Matcher matcherIf = KeyWord.IF.matcher(s);
        if (matcherIf.find()) {
          iterator.remove();
          matchIf = true;
          deque.push(KeyWord.IF);
          needWrite = is(matcherIf.group(2), matcherIf.group(1).length());
          hasTrueCondition = needWrite;
        }
      } else {
        if (hasTrueCondition) {
          Matcher matcherElseIf = KeyWord.ELSE_IF.matcher(s);
          if (KeyWord.ENDIF.matcher(s).matches()) {
            iterator.remove();
            matchIf = false;
            deque.pop();
            hasTrueCondition = false;
            needWrite = true;
          } else if (KeyWord.ELSE.matcher(s).matches()) {
            iterator.remove();
            needWrite = !needWrite;
          } else if (matcherElseIf.find()) {
            iterator.remove();
            needWrite = false;
          } else if (!needWrite) {
            iterator.remove();
          }
        } else {
          Matcher matcherElseIf = KeyWord.ELSE_IF.matcher(s);
          if (KeyWord.ENDIF.matcher(s).matches()) {
            iterator.remove();
            matchIf = false;
            deque.pop();
            hasTrueCondition = false;
            needWrite = true;
          } else if (KeyWord.ELSE.matcher(s).matches()) {
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
    if (!deque.isEmpty()) {
      return "directive #if does not have #endif";
    } else {
      return null;
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
