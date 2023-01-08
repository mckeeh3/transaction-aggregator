package io.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import kalix.springsdk.testkit.EventSourcedTestKit;

public class IntervalEntityTest {
  @Test
  public void updateSubIntervalTest() {
    var testKit = EventSourcedTestKit.of(IntervalEntity::new);

    var epochTime = EpochTime.now().toTransaction();
    var intervalKey = intervalKeyFor(epochTime.toLevelUp());
    {
      var subIntervalKey = intervalKeyFor(epochTime);
      var payload = payloadFor(epochTime, 400.0);
      var subIntervalState = new IntervalEntity.State(subIntervalKey, payload, List.of(), false);
      var command = new IntervalEntity.UpdateSubIntervalCommand(intervalKey, subIntervalState);

      var result = testKit.call(p -> p.updateSubInterval(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(2, events.size());

      var event1 = result.getNextEventOfType(IntervalEntity.IntervalUpdatedEvent.class);
      assertEquals(intervalKey, event1.interval().key());

      var event2 = result.getNextEventOfType(IntervalEntity.SubIntervalUpdatedEvent.class);
      assertEquals(subIntervalKey, event2.subInterval().key());

      var state = testKit.getState();
      assertEquals(intervalKey, state.key());
      assertEquals(payload.amount(), state.payload().amount());
      assertEquals(1, state.subIntervals().size());
      assertEquals(payload, state.subIntervals().get(0).payload());
    }
  }

  @Test
  public void multipleUpdateSubIntervalsTest() {
    var testKit = EventSourcedTestKit.of(IntervalEntity::new);

    var epochTime = EpochTime.now().toTransaction();
    var intervalKey = intervalKeyFor(epochTime.toLevelUp());
    var amount1 = 400.0;
    var amount2 = 500.0;
    {
      var subIntervalKey = intervalKeyFor(epochTime);
      var payload = payloadFor(epochTime, amount1);
      var subIntervalState = new IntervalEntity.State(subIntervalKey, payload, List.of(), false);
      var command = new IntervalEntity.UpdateSubIntervalCommand(intervalKey, subIntervalState);

      var result = testKit.call(p -> p.updateSubInterval(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(2, events.size());

      var event1 = result.getNextEventOfType(IntervalEntity.IntervalUpdatedEvent.class);
      assertEquals(intervalKey, event1.interval().key());

      var event2 = result.getNextEventOfType(IntervalEntity.SubIntervalUpdatedEvent.class);
      assertEquals(subIntervalKey, event2.subInterval().key());

      var state = testKit.getState();
      assertEquals(intervalKey, state.key());
      assertEquals(amount1, state.payload().amount());
      assertEquals(1, state.subIntervals().size());
      assertEquals(payload.amount(), state.subIntervals().get(0).payload().amount());
    }

    {
      var subIntervalKey = intervalKeyFor(epochTime.minus(1));
      var payload = payloadFor(epochTime, amount2);
      var subIntervalState = new IntervalEntity.State(subIntervalKey, payload, List.of(), false);
      var command = new IntervalEntity.UpdateSubIntervalCommand(intervalKey, subIntervalState);

      var result = testKit.call(p -> p.updateSubInterval(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());

      var event = result.getNextEventOfType(IntervalEntity.SubIntervalUpdatedEvent.class);
      assertEquals(subIntervalKey, event.subInterval().key());

      var state = testKit.getState();
      assertEquals(intervalKey, state.key());
      assertEquals(amount1 + amount2, state.payload().amount());
      assertEquals(2, state.subIntervals().size());
      assertEquals(amount1, state.subIntervals().get(0).payload().amount());
      assertEquals(amount2, state.subIntervals().get(1).payload().amount());
    }
  }

  private Payload payloadFor(EpochTime epochTime, double amount) {
    return new Payload(payloadKeyFor(epochTime), amount);
  }

  private PayloadKey payloadKeyFor(EpochTime epochTime) {
    return new PayloadKey(
        "merchantId",
        "serviceCode",
        "accountFrom",
        "accountTo",
        epochTime);
  }

  private IntervalKey intervalKeyFor(EpochTime epochTime) {
    return epochTime.level().equals(EpochTime.Level.transaction)
        ? new IntervalKey(merchantKey(), "transactionId", epochTime)
        : new IntervalKey(merchantKey(), "", epochTime);
  }

  private MerchantKey merchantKey() {
    return new MerchantKey(
        "merchantId",
        "serviceCode",
        "accountFrom",
        "accountTo");
  }
}