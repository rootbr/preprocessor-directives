package org.rootbr.preprocessor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private static final String PATH_TO_SOURCE = "path-to-source";
  private static final String PATH_TO_DEFINED_SYMBOLS = "path-to-defined-symbols";
  private static final String PATH_TO_OUTPUT = "path-to-output";

  public static void main(String[] args) throws IOException {
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

      final DefinedSymbols definedSymbols = new DefinedSymbols(
          parse.getOptionValue(PATH_TO_DEFINED_SYMBOLS)
      );

      final var threads = Runtime.getRuntime().availableProcessors() * 10;
      final var pool = Executors.newFixedThreadPool(threads);

      Pattern pattern = Pattern.compile("^[\\s\\t]*#if[\\s\\t]+(!*?)(\\w+?)[\\s\\t]*$");
      try (Stream<Path> walk = Files.walk(Paths.get(parse.getOptionValue(PATH_TO_SOURCE)), FileVisitOption.FOLLOW_LINKS)) {
        walk.filter(f -> Files.isRegularFile(f) && f.toFile().getAbsolutePath().endsWith(".cs"))
            .forEach(f -> pool.execute(() -> {
              try (Stream<String> stream = Files.lines(f, Charset.forName("UTF-8"))) {
                stream.forEachOrdered(s -> {
                  Matcher matcher = pattern.matcher(s);
                  if (matcher.find()) {
                    System.out.println(f.toString() + " " + matcher.group(1) + " " + matcher.group(2));
                  }
                });
              } catch (MalformedInputException e) {
                log.error(e.getMessage(), e);
              } catch (IOException e) {
                log.warn(e.getMessage(), e);
              }
            }));
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      } finally {
        pool.shutdown();
      }

    } catch (ParseException e) {
      new HelpFormatter().printHelp("preprocessor-directives-utility", options);
      log.error(e.getMessage(), e);
      System.exit(-1);
    }
  }
}
