package org.rootbr.preprocessor.engine;

import java.util.Objects;

public class Instruction {
  final KeyPattern key;
  final boolean needWrite;
  final boolean wasTrueCondition;
  final int lineNumber;
  final String line;

  public Instruction(KeyPattern key, int lineNumber, String line) {
    this.key = key;
    this.lineNumber = lineNumber;
    this.needWrite = false;
    this.wasTrueCondition = false;
    this.line = line;
  }

  public Instruction(KeyPattern key, boolean needWrite, int lineNumber, String line) {
    this.key = key;
    this.needWrite = needWrite;
    this.wasTrueCondition = needWrite;
    this.lineNumber = lineNumber;
    this.line = line;
  }

  public Instruction(KeyPattern key, boolean needWrite, boolean wasTrueCondition, int lineNumber, String line) {
    this.key = key;
    this.needWrite = needWrite;
    this.wasTrueCondition = wasTrueCondition;
    this.lineNumber = lineNumber;
    this.line = line;
  }

  @Override
  public String toString() {
    return "{key=" + key + ", lineNumber=" + lineNumber + ", line='" + line + "'}";
  }

  public KeyPattern getKey() {
    return key;
  }

  public boolean isNeedWrite() {
    return needWrite;
  }

  public boolean isWasTrueCondition() {
    return wasTrueCondition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Instruction that = (Instruction) o;
    return needWrite == that.needWrite
        && wasTrueCondition == that.wasTrueCondition
        && key == that.key;
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, needWrite, wasTrueCondition);
  }
}
