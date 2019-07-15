package org.rootbr.preprocessor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.rootbr.preprocessor.engine.workers.DirectiveProcessingAndCopyWorker;
import org.rootbr.preprocessor.engine.FileProcessingEngine;
import org.rootbr.preprocessor.engine.workers.Workers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);
  private static final String PATH_TO_SOURCE = "path-to-source";
  private static final String PATH_TO_DEFINED_SYMBOLS = "path-to-defined-symbols";
  private static final String PATH_TO_OUTPUT = "path-to-output";

  private static final Properties properties = new Properties();

  public static void main(String[] args) throws IOException {

    long start = System.nanoTime();

    CommandLine parse = parseCommandLine(args);

    try (InputStream input = new FileInputStream(parse.getOptionValue(PATH_TO_DEFINED_SYMBOLS))) {
      properties.load(new InputStreamReader(input, UTF_8));
    }

    final var workers = new Workers(new DirectiveProcessingAndCopyWorker(properties));

    new FileProcessingEngine(workers).processingFolder(
        Paths.get(parse.getOptionValue(PATH_TO_SOURCE)).normalize(),
        Paths.get(parse.getOptionValue(PATH_TO_OUTPUT)).normalize()
    );

    log.warn("time of processing is {} ms", (System.nanoTime() - start) / 1_000_000L);
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
