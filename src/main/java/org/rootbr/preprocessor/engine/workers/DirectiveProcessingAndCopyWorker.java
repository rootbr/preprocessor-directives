package org.rootbr.preprocessor.engine.workers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.rootbr.preprocessor.engine.KeyPattern.ELSE;
import static org.rootbr.preprocessor.engine.KeyPattern.ELSE_IF;
import static org.rootbr.preprocessor.engine.KeyPattern.IF;

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
import org.mozilla.universalchardet.UniversalDetector;
import org.rootbr.preprocessor.engine.Instruction;
import org.rootbr.preprocessor.engine.KeyPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectiveProcessingAndCopyWorker implements Worker {
  private static final Logger log = LoggerFactory.getLogger(DirectiveProcessingAndCopyWorker.class);

  private final Properties properties;

  public DirectiveProcessingAndCopyWorker(Properties properties) {
    this.properties = properties;
  }

  @Override
  public void processinFile(Path from, Path to) {
    final var charset = detectCharset(from);
    try {
      final var lines = Files.readAllLines(from, charset);
      final var result = processingDirective(lines);
      if (result.isSuccess) {
        log.info("{}: {}", from, result.message);
      } else {
        log.warn("{}: {}", from, result.message);
      }
      Files.write(to, lines, charset, StandardOpenOption.CREATE);
    } catch (IOException e) {
      log.error("{}, encoding: {}, message: {}", from.toAbsolutePath(), charset, e.getMessage());
    }
  }

  private Charset detectCharset(Path f) {
    String encoding = null;
    try {
      encoding = UniversalDetector.detectCharset(f);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    if (encoding == null) {
      log.warn("{} - No encoding detected, try to use UTF-8", f.toAbsolutePath());
      return UTF_8;
    } else {
      return Charset.forName(encoding);
    }
  }

  static class ResultProcessing {
    public final String message;
    public final boolean isSuccess;
    public final boolean wasProcessed;

    private ResultProcessing(String message, boolean isSuccess, boolean wasProcessed) {
      this.message = message;
      this.isSuccess = isSuccess;
      this.wasProcessed = wasProcessed;
    }
  }

  public ResultProcessing processingDirective(List<String> lines) {
    boolean wasProcessed = false;
    final var iterator = lines.listIterator();
    Deque<Instruction> deque = new ArrayDeque<>();
    while (iterator.hasNext()) {
      final var i = iterator.nextIndex();
      final var s = iterator.next();
      if (deque.isEmpty()) {
        if (findStartAndPush(i, s, deque)) {
          wasProcessed = true;
          iterator.remove();
        }
      } else {
        final var peek = deque.peek();
        switch (peek.getKey()) {
          case RAW_STRING:
          case COMMENT:
            if (!findEndAndPop(i, peek.getKey().end(s), deque)) {
              removeLineIfNecessary(iterator, deque);
            } else {
              log.debug("end {}=\"{}\"", peek.getKey(), s);
            }
            break;

          case ELSE:
            if (processingEndIf(s, deque)) {
              iterator.remove();
            } else {
              removeLineIfNecessary(iterator, deque);
            }
            break;

          case IF:
          case ELSE_IF:
            if (processingEndIf(s, deque)) {
              iterator.remove();
            } else if (processingElse(i, s, deque)) {
              iterator.remove();
            } else if (processingElseIf(i, s, deque)) {
              iterator.remove();
            } else {
              removeLineIfNecessary(iterator, deque);
            }
            break;
        }
      }
    }
    return new ResultProcessing(
        deque.isEmpty()
            ? "was processed successfully"
            : "was processed unsuccessfully: " + Arrays.toString(deque.toArray()),
        deque.isEmpty(),
        wasProcessed
    );
  }

  private void removeLineIfNecessary(Iterator<String> iterator, Deque<Instruction> deque) {
    if (!deque.isEmpty()
        && (deque.peek().getKey() == IF || deque.peek().getKey() == ELSE || deque.peek().getKey() == ELSE_IF)
        && !deque.peek().isNeedWrite()
    ) {
      iterator.remove();
    }
  }

  private boolean processingElseIf(int number, String s, Deque<Instruction> deque) {
    final var matcherElseIf = ELSE_IF.start(s);
    if (matcherElseIf.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      final var condition = !peek.isWasTrueCondition()
          && condition(matcherElseIf.group(2), matcherElseIf.group(1).length());
      deque.push(new Instruction(
          ELSE_IF,
          condition,
          peek.isWasTrueCondition() || condition,
          number,
          s
      ));
      return true;
    }
    return false;
  }

  private boolean processingElse(int number, String s, Deque<Instruction> deque) {
    final var matcherElse = ELSE.start(s);
    if (matcherElse.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      deque.push(new Instruction(ELSE, !peek.isNeedWrite(), number, s));
      return true;
    }
    return false;
  }

  private boolean processingEndIf(String s, Deque<Instruction> deque) {
    final var matcherEnd = IF.end(s);
    if (matcherEnd.find()) {
      while (Objects.requireNonNull(deque.peek()).getKey() != IF) {
        deque.pop();
      }
      deque.pop();
      return true;
    }
    return false;
  }

  private boolean findStartAndPush(
      int number, final String s, final Deque<Instruction> deque
  ) {
    if (KeyPattern.COMMENT.start(s).find()) {
      deque.push(new Instruction(KeyPattern.COMMENT, number, s));
      log.debug("start COMMENT=\"{}\"", s);
      return false;
    }

    if (KeyPattern.RAW_STRING.start(s).find()) {
      deque.push(new Instruction(KeyPattern.RAW_STRING, number, s));
      log.debug("start RAW_STRING=\"{}\"", s);
      return false;
    }

    final var matcher = IF.start(s);
    if (matcher.find()) {
      log.debug("start IF=\"{}\"", s);
      deque.push(new Instruction(
          IF,
          condition(matcher.group(2), matcher.group(1).length()),
          number,
          s
      ));
      return true;
    }
    return false;
  }

  private boolean findEndAndPop(
      int number, final Matcher matcher, final Deque<Instruction> deque
  ) {
    if (matcher.find()) {
      deque.pop();

      final var s = matcher.replaceAll("");
      if (KeyPattern.COMMENT.start(s).find()) {
        deque.push(new Instruction(KeyPattern.COMMENT, number, s));
      }

      if (KeyPattern.RAW_STRING.start(s).find()) {
        deque.push(new Instruction(KeyPattern.RAW_STRING, number, s));
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
