package io.aggregator;

record EpochTime(long value, Level level) {
  enum Level {
    day("day"),
    hour("hour"),
    minute("minute"),
    second("second"),
    millisecond("millisecond"),
    stripedSecond("stripedSecond"),
    transaction("transaction");

    final String level;

    private Level(String level) {
      this.level = level;
    }
  }

  static EpochTime empty() {
    return new EpochTime(0, Level.day);
  }

  static EpochTime now() {
    return new EpochTime(System.currentTimeMillis(), Level.millisecond);
  }

  EpochTime toDay() {
    return toMs().fromMsTo(Level.day);
  }

  EpochTime minus(int amount) {
    return new EpochTime(value - amount, level);
  }

  String entityId() {
    return "%s_%s".formatted(value, level.level);
  }

  private EpochTime toMs() {
    switch (level) {
    case day:
      return new EpochTime(value * 24 * 60 * 60 * 1000, Level.millisecond);
    case hour:
      return new EpochTime(value * 60 * 60 * 1000, Level.millisecond);
    case minute:
      return new EpochTime(value * 60 * 1000, Level.millisecond);
    case second:
      return new EpochTime(value * 1000, Level.millisecond);
    case millisecond:
      return this;
    case stripedSecond:
      return new EpochTime(value * 1000, Level.millisecond);
    case transaction:
      return new EpochTime(value * 1000, Level.millisecond);
    default:
      throw new RuntimeException("Unknown level: " + level);
    }
  }

  private EpochTime fromMsTo(Level level) {
    switch (level) {
    case day:
      return new EpochTime(value / (24 * 60 * 60 * 1000), Level.day);
    case hour:
      return new EpochTime(value / (60 * 60 * 1000), Level.hour);
    case minute:
      return new EpochTime(value / (60 * 1000), Level.minute);
    case second:
      return new EpochTime(value / 1000, Level.second);
    case millisecond:
      return new EpochTime(value, Level.millisecond);
    case stripedSecond:
      return new EpochTime(value / 1000, Level.stripedSecond);
    case transaction:
      return new EpochTime(value / 1000, Level.transaction);
    default:
      throw new RuntimeException("Unknown level: " + level);
    }
  }
}
