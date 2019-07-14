package org.rootbr.preprocessor;

import java.util.Objects;

public class ElementWrapper {
  final KeyPattern key;
  final boolean needWrite;
  final boolean wasTrueCondition;

  public ElementWrapper(KeyPattern key) {
    this.key = key;
    this.needWrite = false;
    this.wasTrueCondition = false;
  }

  public ElementWrapper(KeyPattern key, boolean needWrite) {
    this.key = key;
    this.needWrite = needWrite;
    this.wasTrueCondition = needWrite;
  }

  public ElementWrapper(KeyPattern key, boolean needWrite, boolean wasTrueCondition) {
    this.key = key;
    this.needWrite = needWrite;
    this.wasTrueCondition = wasTrueCondition;
  }

  @Override
  public String toString() {
    return key.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ElementWrapper that = (ElementWrapper) o;
    return needWrite == that.needWrite &&
        wasTrueCondition == that.wasTrueCondition &&
        key == that.key;
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, needWrite, wasTrueCondition);
  }
}
