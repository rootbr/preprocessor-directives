package org.rootbr.preprocessor;

import static org.rootbr.preprocessor.KeyPattern.ELSE;
import static org.rootbr.preprocessor.KeyPattern.ELSE_IF;
import static org.rootbr.preprocessor.KeyPattern.IF;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
        log.warn("file {} conditionIf wrong ({})", f, s);
      }
      log.info("write file={}", t.toAbsolutePath());
      Files.write(t, lines, charset, StandardOpenOption.CREATE);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }

  public String executeDirective(List<String> lines) {
    final var iterator = lines.iterator();
    Deque<ElementWrapper> deque = new ArrayDeque<>();

    while (iterator.hasNext()) {

      final var s = iterator.next();

      if (deque.isEmpty()) {
        if (findStartAndPush(s, deque)) {
          iterator.remove();
        }
      } else {
        final var peek = deque.peek();
        switch (peek.key) {
          case RAW_STRING:
          case COMMENT:
            if (!findEndAndPop(peek.key.end(s), deque)) {
              processingLine(iterator, deque);
            } else {
              log.debug("end {}=\"{}\"", peek.key, s);
            }
            break;

          case ELSE:
            if (processinEndIf(s, deque)) {
              iterator.remove();
            } else {
              processingLine(iterator, deque);
            }
            break;

          case IF:
          case ELSE_IF:
            if (processinEndIf(s, deque)) {
              iterator.remove();
            } else if (processingElse(s, deque)) {
              iterator.remove();
            } else if (processingElseIf(s, deque)) {
              iterator.remove();
            } else {
              processingLine(iterator, deque);
            }
            break;
        }
      }
    }
    if (!deque.isEmpty()) {
      return Arrays.toString(deque.toArray());
    } else {
      return null;
    }
  }

  private void processingLine(Iterator<String> iterator, Deque<ElementWrapper> deque) {
    if (!deque.isEmpty()
        && (deque.peek().key == IF || deque.peek().key == ELSE || deque.peek().key == ELSE_IF)
        && !deque.peek().needWrite
    ) {
      iterator.remove();
    }
  }

  private boolean processingElseIf(String s, Deque<ElementWrapper> deque) {


    final var matcherElseIf = KeyPattern.ELSE_IF.start(s);
    if (matcherElseIf.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      final var condition = !peek.wasTrueCondition
          && condition(matcherElseIf.group(2), matcherElseIf.group(1).length());
      deque.push(new ElementWrapper(
          KeyPattern.ELSE_IF,
          condition,
          peek.wasTrueCondition || condition
      ));
      return true;
    }
    return false;
  }

  private boolean processingElse(String s, Deque<ElementWrapper> deque) {
    final var matcherElse = KeyPattern.ELSE.start(s);
    if (matcherElse.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      deque.push(new ElementWrapper(KeyPattern.ELSE, !peek.needWrite));
      return true;
    }
    return false;
  }

  private boolean processinEndIf(String s, Deque<ElementWrapper> deque) {
    final var matcherEnd = IF.end(s);
    if (matcherEnd.find()) {
      while (Objects.requireNonNull(deque.peek()).key != IF) {
        deque.pop();
      }
      deque.pop();
      return true;
    }
    return false;
  }

  private boolean findStartAndPush(
      final String s, final Deque<ElementWrapper> deque
  ) {
    if (KeyPattern.COMMENT.start(s).find()) {
      deque.push(new ElementWrapper(KeyPattern.COMMENT));
      log.debug("start COMMENT=\"{}\"", s);
      return false;
    }

    if (KeyPattern.RAW_STRING.start(s).find()) {
      deque.push(new ElementWrapper(KeyPattern.RAW_STRING));
      log.debug("start RAW_STRING=\"{}\"", s);
      return false;
    }

    final var matcher = IF.start(s);
    if (matcher.find()) {
      log.debug("start IF=\"{}\"", s);
      deque.push(new ElementWrapper(
          IF,
          condition(matcher.group(2), matcher.group(1).length())
      ));
      return true;
    }
    return false;
  }

  private boolean findEndAndPop(
      final Matcher matcher, final Deque<ElementWrapper> deque
  ) {
    if (matcher.find()) {
      deque.pop();

      final var s = matcher.replaceAll("");
      if (KeyPattern.COMMENT.start(s).find()) {
        deque.push(new ElementWrapper(KeyPattern.COMMENT));
      }

      if (KeyPattern.RAW_STRING.start(s).find()) {
        deque.push(new ElementWrapper(KeyPattern.RAW_STRING));
      }
      return true;
    } else {
      return false;
    }
  }

  private boolean condition(String key, int numberOfNegation) {
    var b = Boolean.parseBoolean(properties.getProperty(key));
    while (numberOfNegation-- > 0) {
      b = !b;
    }
    return b;
  }
}
