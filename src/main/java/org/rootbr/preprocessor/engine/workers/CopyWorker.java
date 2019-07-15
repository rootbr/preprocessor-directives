package org.rootbr.preprocessor.engine.workers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyWorker implements Worker {
  private static final Logger log = LoggerFactory.getLogger(CopyWorker.class);

  @Override
  public void processingFile(Path from, Path to) {
    try {
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("{} - message: {}", from.toAbsolutePath(), e.getMessage());
    }
  }
}
