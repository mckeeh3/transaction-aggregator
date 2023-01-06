package io.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import kalix.springsdk.testkit.EventSourcedTestKit;

public class MerchantEntityTest {
  @Test
  public void createMerchantTest() {
    var testKit = EventSourcedTestKit.of(MerchantEntity::new);

    {
      var merchantKey = merchantKey();
      var command = new MerchantEntity.CreateMerchantCommand(merchantKey);

      var result = testKit.call(p -> p.createMerchant(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(MerchantEntity.MerchantCreatedEvent.class);
      assertEquals(merchantKey, event.key());

      var state = testKit.getState();
      assertEquals(merchantKey, state.key());
    }
  }

  @Test
  public void updateDayTest() {
    var testKit = EventSourcedTestKit.of(MerchantEntity::new);

    {
      var merchantKey = merchantKey();
      var command = new MerchantEntity.CreateMerchantCommand(merchantKey);

      var result = testKit.call(p -> p.createMerchant(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(0, 100.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(MerchantEntity.DayUpdatedEvent.class);
      var expectedPaymentKey = new PaymentKey("1", merchantKey());
      var expectedPayload = payloadFor(0, 100.0);
      assertEquals(expectedPaymentKey, event.key());
      assertEquals(expectedPayload, event.payload());

      var state = testKit.getState();
      assertEquals(merchantKey(), state.key());
    }
  }

  @Test
  public void updateDayWithExistingPaymentTest() {
    var testKit = EventSourcedTestKit.of(MerchantEntity::new);

    {
      var merchantKey = merchantKey();
      var command = new MerchantEntity.CreateMerchantCommand(merchantKey);

      var result = testKit.call(p -> p.createMerchant(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(0, 100.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(0, 200.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(MerchantEntity.DayUpdatedEvent.class);
      var expectedPaymentKey = new PaymentKey("1", merchantKey());
      var expectedPayload = payloadFor(0, 200.0);
      assertEquals(expectedPaymentKey, event.key());
      assertEquals(expectedPayload, event.payload());

      var state = testKit.getState();
      assertEquals(merchantKey(), state.key());
    }
  }

  @Test
  public void startNextPaymentCycleTest() {
    var testKit = EventSourcedTestKit.of(MerchantEntity::new);

    {
      var merchantKey = merchantKey();
      var command = new MerchantEntity.CreateMerchantCommand(merchantKey);

      var result = testKit.call(p -> p.createMerchant(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(0, 100.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(1, 200.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new MerchantEntity.StartNextPaymentCycleCommand(merchantKey());

      var result = testKit.call(p -> p.startNextPaymentCycle(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());

      var event = result.getNextEventOfType(MerchantEntity.NextPaymentCycleStartedEvent.class);
      var expectedPriorKey = new PaymentKey("1", merchantKey());
      var expectedNextKey = new PaymentKey("2", merchantKey());
      assertEquals(expectedPriorKey, event.priorKey());
      assertEquals(expectedNextKey, event.nextKey());
    }

    {
      var command = new MerchantEntity.UpdateDayCommand(payloadFor(2, 300.0));

      var result = testKit.call(p -> p.updateDay(command));
      assertEquals("OK", result.getReply());

      var event = result.getNextEventOfType(MerchantEntity.DayUpdatedEvent.class);
      var expectedPaymentKey = new PaymentKey("2", merchantKey());
      var expectedPayload = payloadFor(2, 300.0);
      assertEquals(expectedPaymentKey, event.key());
      assertEquals(expectedPayload, event.payload());
    }
  }

  private MerchantKey merchantKey() {
    return new MerchantKey("merchantId", "serviceCode", "accountFrom", "accountTo");
  }

  private Payload payloadFor(int daysMinus, double amount) {
    return new Payload(payloadKeyFor(daysMinus), amount);
  }

  private PayloadKey payloadKeyFor(int daysMinus) {
    return new PayloadKey(
        "merchantId",
        "serviceCode",
        "accountFrom",
        "accountTo",
        EpochTime.now().toDay().minus(daysMinus));
  }
}
