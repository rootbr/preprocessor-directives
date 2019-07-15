package org.rootbr.preprocessor.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.rootbr.preprocessor.engine.workers.Workers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileProcessingEngine {

  private static final Logger log = LoggerFactory.getLogger(FileProcessingEngine.class);

  private final Workers workers;

  public FileProcessingEngine(Workers workers) {
    this.workers = workers;
  }

  public void processingFolder(Path folderSource, Path folderDestination) {
    final var pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );
    try (Stream<Path> walk = Files.walk(folderSource)) {
      walk.filter(Files::isRegularFile)
          .forEach(from -> pool.execute(() -> {
                final var to = folderDestination.resolve(
                    from.subpath(folderSource.getNameCount(), from.getNameCount())
                );
                createParentFolderIfNotExist(to);
                workers.getWorker(from).processingFile(from, to);
              }
          ));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    awaitPool(pool);
  }

  private void createParentFolderIfNotExist(Path to) {
    Path parentDir = to.getParent();
    if (!Files.exists(parentDir)) {
      try {
        Files.createDirectories(parentDir);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  private void awaitPool(ThreadPoolExecutor pool) {
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
}
