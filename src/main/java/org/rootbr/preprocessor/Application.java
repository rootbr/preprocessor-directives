package org.rootbr.preprocessor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);
  private static final String PATH_TO_SOURCE = "path-to-source";
  private static final String PATH_TO_DEFINED_SYMBOLS = "path-to-defined-symbols";
  private static final String PATH_TO_OUTPUT = "path-to-output";

  public static void main(String[] args) throws IOException {

    CommandLine parse = parseCommandLine(args);

    final var processingFile = new ProcessingFile(parse.getOptionValue(PATH_TO_DEFINED_SYMBOLS));

    final var threads = Runtime.getRuntime().availableProcessors() * 10;
    final var pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

    final var rootPathSource = Paths.get(parse.getOptionValue(PATH_TO_SOURCE)).normalize();

    final var rootPathDestination = Paths.get(parse.getOptionValue(PATH_TO_OUTPUT)).normalize();
    try (Stream<Path> walk = Files.walk(rootPathSource)) {
      walk.filter(Files::isRegularFile)
          .forEach(f -> pool.execute(() -> {
                final var to = rootPathDestination.resolve(f.subpath(rootPathSource.getNameCount(), f.getNameCount()));
                createParentFolderIfNotExist(to);

                if (f.toFile().getAbsolutePath().endsWith(".cs")) {
                  try {
                    processingFile.processingFile(f, to, UTF_8);
                  } catch (UncheckedIOException e) {
                    String encoding = detectCharset(f);
                    if (encoding != null) {
                      processingFile.processingFile(f, to, Charset.forName(encoding));
                    }
                  }
                } else {
                  try {
                    Files.copy(f, to, StandardCopyOption.REPLACE_EXISTING);
                  } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                  }
                }
              }
          ));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    awaitPool(pool);
  }

  private static void createParentFolderIfNotExist(Path to) {
    Path parentDir = to.getParent();
    if (!Files.exists(parentDir)) {
      try {
        Files.createDirectories(parentDir);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  private static String detectCharset(Path f) {
    try {
      return UniversalDetector.detectCharset(f);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    log.warn("No encoding detected for file: {}", f);
    return null;
  }


  private static void awaitPool(ThreadPoolExecutor pool) {
    pool.shutdown();
    try {
      if (!pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        pool.shutdownNow();
      }
    } catch (InterruptedException ex) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private static CommandLine parseCommandLine(String[] args) {
    Options options = new Options()
        .addOption(Option.builder()
            .longOpt(PATH_TO_SOURCE)
            .hasArg().required()
            .desc("Путь к папке, в которой содержатся файлы с исходным кодом на языке C#")
            .build()
        ).addOption(Option.builder()
            .longOpt(PATH_TO_DEFINED_SYMBOLS)
            .hasArg().required()
            .desc("Путь к тестовому файлу, содержащему набором символов (defined symbols)")
            .build()
        ).addOption(Option.builder()
            .longOpt(PATH_TO_OUTPUT)
            .hasArg().required()
            .desc("Путь к папке, в которую необходимо поместить обработанные файлы")
            .build()
        );

    try {
      return new DefaultParser().parse(options, args);
    } catch (ParseException e) {
      new HelpFormatter().printHelp("preprocessor-directives-utility", options);
      log.error(e.getMessage(), e);
      System.exit(-1);
      return null;
    }
  }
}
