package org.rootbr.preprocessor.engine.workers;

import java.nio.file.Path;

public interface Worker {
  void processinFile(Path from, Path to);
}
