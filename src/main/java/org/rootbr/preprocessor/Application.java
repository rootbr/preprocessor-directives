package org.rootbr.preprocessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);
  public static final String PATH_TO_SOURCE = "path-to-source";
  public static final String PATH_TO_DEFINED_SYMBOLS = "path-to-defined-symbols";
  public static final String PATH_TO_OUTPUT = "path-to-output";
  public static final Properties DEFINED_SYMBOLS = new Properties();

  public static void main(String[] args) {
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
      final var parse = new DefaultParser().parse(options, args);

      try (InputStream input = new FileInputStream(parse.getOptionValue(PATH_TO_DEFINED_SYMBOLS))) {
        // TODO add description
        DEFINED_SYMBOLS.load(new InputStreamReader(input, Charset.forName("UTF-8")));
      }

      final var threads = Runtime.getRuntime().availableProcessors() * 10;
      final var pool = Executors.newFixedThreadPool(threads);

      try (Stream<Path> walk = Files.walk(Paths.get(parse.getOptionValue(PATH_TO_SOURCE)))) {
        walk.filter(f -> Files.isRegularFile(f) && f.getFileName().toString().endsWith(".cs"))
            .forEach(f -> pool.execute(() -> System.out.println(f)));
      } catch (IOException e) {
        log.error(e.getMessage());
      } finally {
        pool.shutdown();
      }

    } catch (ParseException e) {
      new HelpFormatter().printHelp("preprocessor-directives-utility", options);
      exitApplication(e.getMessage());
    } catch (IOException e) {
      exitApplication(e.getMessage());
    }
  }

  private static void exitApplication(String message) {
    log.error(message);
    System.exit(-1);
  }
}
