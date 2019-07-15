package org.rootbr.preprocessor.engine.workers;

import java.nio.file.Path;

public interface Worker {
  void processingFile(Path from, Path to);
}
