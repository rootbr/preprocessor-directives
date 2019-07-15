package org.rootbr.preprocessor.engine.workers;

import java.nio.file.Path;

public class Workers {
  private final Worker copy = new CopyWorker();
  private final Worker directiveProcessingAndCopy;

  public Workers(Worker directiveProcessingAndCopy) {
    this.directiveProcessingAndCopy = directiveProcessingAndCopy;
  }

  public Worker getWorker(Path from) {
    final var absolutePath = from.toFile().getAbsolutePath();
    if (absolutePath.endsWith(".cs")) {
      return directiveProcessingAndCopy;
    } else {
      return copy;
    }
  }
}
