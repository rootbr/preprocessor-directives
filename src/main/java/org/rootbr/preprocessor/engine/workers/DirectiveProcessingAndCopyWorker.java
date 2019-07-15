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
import java.util.List;
import java.util.ListIterator;
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
  public void processingFile(Path from, Path to) {
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

  static final class ResultProcessing {
    public final String message;
    public final boolean isSuccess;

    private ResultProcessing(String message, boolean isSuccess) {
      this.message = message;
      this.isSuccess = isSuccess;
    }
  }

  public ResultProcessing processingDirective(List<String> lines) {
    final var iterator = lines.listIterator();
    Deque<Instruction> deque = new ArrayDeque<>();
    while (iterator.hasNext()) {
      final var line = iterator.next();
      if (deque.isEmpty()) {
        processingStartInstruction(iterator, line, deque);
      } else {
        final var peek = deque.peek();
        switch (peek.getKey()) {
          case RAW_STRING:
          case COMMENT:
            processingEndInstructionsCommentOrRawString(iterator, line, peek.getKey(), deque);
            break;

          case ELSE:
            if (!processingEndInstructionIf(iterator, line, deque)) {
              removeLineIfNecessary(iterator, deque);
            }
            break;

          case IF:
          case ELSE_IF:
            if (!processingEndInstructionIf(iterator, line, deque)
                && !processingElse(iterator, line, deque)
                && !processingElseIf(iterator, line, deque)
            ) {
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
        deque.isEmpty()
    );
  }

  private void removeLineIfNecessary(ListIterator iterator, Deque<Instruction> deque) {
    if (!deque.isEmpty() && !deque.peek().isNeedWrite()) {
      iterator.remove();
    }
  }

  private boolean processingElseIf(
      final ListIterator iterator,
      final String line,
      final Deque<Instruction> deque
  ) {
    final var matcherElseIf = ELSE_IF.start(line);
    if (matcherElseIf.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      final var condition = !peek.isWasTrueCondition()
          && condition(matcherElseIf.group(2), matcherElseIf.group(1).length());
      deque.push(new Instruction(
          ELSE_IF,
          condition,
          peek.isWasTrueCondition() || condition,
          iterator.previousIndex(),
          line
      ));
      iterator.remove();
      return true;
    }
    return false;
  }

  private boolean processingElse(
      final ListIterator iterator,
      final String line,
      final Deque<Instruction> deque
  ) {
    final var matcherElse = ELSE.start(line);
    if (matcherElse.find()) {
      final var peek = Objects.requireNonNull(deque.peek());
      deque.push(new Instruction(ELSE, !peek.isNeedWrite(), iterator.previousIndex(), line));
      iterator.remove();
      return true;
    }
    return false;
  }

  private boolean processingEndInstructionIf(
      final ListIterator iterator,
      final String line,
      final Deque<Instruction> deque
  ) {
    final var matcherEnd = IF.end(line);
    if (matcherEnd.find()) {
      while (Objects.requireNonNull(deque.peek()).getKey() != IF) {
        deque.pop();
      }
      deque.pop();
      iterator.remove();
      return true;
    }
    return false;
  }

  private void processingEndInstructionsCommentOrRawString(
      final ListIterator iterator,
      final String line,
      final KeyPattern pattern,
      final Deque<Instruction> deque
  ) {
    Matcher matcher = pattern.end(line);
    if (matcher.find()) {
      deque.pop();

      final var s = matcher.replaceAll("");
      if (KeyPattern.COMMENT.start(s).find()) {
        deque.push(new Instruction(KeyPattern.COMMENT, iterator.previousIndex(), line));
      }

      if (KeyPattern.RAW_STRING.start(s).find()) {
        deque.push(new Instruction(KeyPattern.RAW_STRING, iterator.previousIndex(), line));
      }
    } else {
      removeLineIfNecessary(iterator, deque);
    }
  }

  private void processingStartInstruction(
      final ListIterator iterator,
      final String line,
      final Deque<Instruction> deque
  ) {

    if (KeyPattern.COMMENT.start(line).find()) {
      deque.push(new Instruction(KeyPattern.COMMENT, iterator.previousIndex(), line));
      log.debug("start COMMENT=\"{}\"", line);

    } else if (KeyPattern.RAW_STRING.start(line).find()) {
      deque.push(new Instruction(KeyPattern.RAW_STRING, iterator.previousIndex(), line));
      log.debug("start RAW_STRING=\"{}\"", line);

    } else {
      final var matcher = IF.start(line);
      if (matcher.find()) {
        deque.push(new Instruction(
            IF,
            condition(matcher.group(2), matcher.group(1).length()),
            iterator.previousIndex(),
            line
        ));
        log.debug("start IF=\"{}\"", line);
        iterator.remove();
      }
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
