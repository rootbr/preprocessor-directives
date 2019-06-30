package org.rootbr.preprocessor;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    Options options = new Options()
        .addOption(Option.builder()
            .longOpt("path-to-source")
            .hasArg().required()
            .desc("Путь к папке, в которой содержатся файлы с исходным кодом на языке C#")
            .build()
        ).addOption(Option.builder()
            .longOpt("path-to-defined-symbols")
            .hasArg().required()
            .desc("Путь к тестовому файлу, содержащему набором символов (defined symbols)")
            .build()
        ).addOption(Option.builder()
            .longOpt("path-to-output")
            .hasArg().required()
            .desc("Путь к папке, в которую необходимо поместить обработанные файлы")
            .build()
        );

    try {
      new DefaultParser().parse(options, args);
    } catch (ParseException e) {
      log.error(e.getMessage());
      System.err.println(e.getMessage());
      new HelpFormatter().printHelp("preprocessor-directives-utility", options);
      System.exit(-1);
    }
  }
}
