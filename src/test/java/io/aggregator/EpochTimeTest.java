package io.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EpochTimeTest {
  @Test
  public void levelUpFromTransactionTest() {
    var timeTransaction = EpochTime.now().toTransaction();
    var timeSubSecond = timeTransaction.toLevelUp();

    assertEquals(EpochTime.Level.subSecond, timeSubSecond.level());
    assertEquals(EpochTime.Level.second, timeSubSecond.toLevelUp().level());
    assertEquals(EpochTime.Level.minute, timeSubSecond.toLevelUp().toLevelUp().level());
    assertEquals(EpochTime.Level.hour, timeSubSecond.toLevelUp().toLevelUp().toLevelUp().level());

    var timeDay = timeSubSecond.toLevelUp().toLevelUp().toLevelUp().toLevelUp();
    assertEquals(EpochTime.Level.day, timeDay.level());
    assertEquals(EpochTime.Level.day, timeDay.toLevelUp().level());
    assertEquals(EpochTime.Level.day, timeDay.toLevelUp().toLevelUp().level());

    assertEquals(timeDay, timeDay.toLevelUp().toLevelUp().toLevelUp().toLevelUp());
  }
}
