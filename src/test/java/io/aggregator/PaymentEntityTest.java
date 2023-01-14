package io.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import kalix.springsdk.testkit.EventSourcedTestKit;

public class PaymentEntityTest {
  @Test
  public void createPaymentTest() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var paymentKey = paymentKeyFor(0);
      var command = new PaymentEntity.CreatePaymentCommand(paymentKey, payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(PaymentEntity.PaymentCreatedEvent.class);
      assertEquals(paymentKey, event.key());
      assertEquals(payloads, event.priorPaymentDays());

      var state = testKit.getState();
      assertEquals(paymentKey, state.key());
      assertEquals(payloads, state.minusDays());
    }
  }

  @Test
  public void updatePaymentTest() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var command = new PaymentEntity.CreatePaymentCommand(paymentKeyFor(0), payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(3, 400.0);
      var paymentKey = paymentKeyFor(0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKey, updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(PaymentEntity.PaymentUpdatedEvent.class);
      assertEquals(paymentKey, event.key());
      assertEquals(updatePayload, event.payload());

      var state = testKit.getState();
      assertEquals(paymentKey, state.key());
      assertEquals(List.of(updatePayload), state.plusDays());
    }
  }

  @Test
  public void getPaymentZeroUpdatesTest() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var command = new PaymentEntity.CreatePaymentCommand(paymentKeyFor(0), payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var payLoadExpected = new Payload(payloadKeyFor(0), 0.0);
      var result = testKit.call(p -> p.getPayment());
      assertEquals(payLoadExpected, result.getReply());
    }
  }

  @Test
  public void getPaymentOneUpdateZeroMinusDays() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var command = new PaymentEntity.CreatePaymentCommand(paymentKeyFor(0), payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(4, 400.0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKeyFor(0), updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var payLoadExpected = new Payload(payloadKeyFor(0), 400.0);
      var result = testKit.call(p -> p.getPayment());
      assertEquals(payLoadExpected, result.getReply());
    }
  }

  @Test
  public void getPaymentTwoUpdatesOneMinusDay() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var command = new PaymentEntity.CreatePaymentCommand(paymentKeyFor(0), payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(3, 400.0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKeyFor(0), updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(4, 500.0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKeyFor(0), updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var payLoadExpected = new Payload(payloadKeyFor(0), 600.0);
      var result = testKit.call(p -> p.getPayment());
      assertEquals(payLoadExpected, result.getReply());
    }
  }

  @Test
  public void nextPaymentCycleStartedTest() {
    var testKit = EventSourcedTestKit.of(PaymentEntity::new);

    {
      var payloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 300.0));
      var command = new PaymentEntity.CreatePaymentCommand(paymentKeyFor(1), payloads);

      var result = testKit.call(p -> p.createPayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(3, 400.0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKeyFor(1), updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var updatePayload = payloadFor(4, 500.0);
      var command = new PaymentEntity.UpdatePaymentCommand(paymentKeyFor(1), updatePayload);

      var result = testKit.call(p -> p.updatePayment(command));
      assertEquals("OK", result.getReply());
    }

    {
      var command = new PaymentEntity.StartNextPaymentCycleCommand(paymentKeyFor(1), paymentKeyFor(0));

      var result = testKit.call(p -> p.startNextPaymentCycle(command));
      assertEquals("OK", result.getReply());

      var events = result.getAllEvents();
      assertEquals(1, events.size());
      var event = result.getNextEventOfType(PaymentEntity.NextPaymentCycleStartedEvent.class);
      assertEquals(paymentKeyFor(1), event.priorKey());
      assertEquals(paymentKeyFor(0), event.nextKey());

      var expectedPayloads = List.of(payloadFor(1, 100.0), payloadFor(2, 200.0), payloadFor(3, 400.0), payloadFor(4, 500.0));
      assertEquals(expectedPayloads, event.priorPaymentDays());
    }
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

  private MerchantKey merchantKeyFor(int daysMinus) {
    return new MerchantKey(
        "merchantId",
        "serviceCode",
        "accountFrom",
        "accountTo");
  }

  private PaymentKey paymentKeyFor(int daysMinus) {
    return new PaymentKey(
        "paymentId",
        merchantKeyFor(daysMinus));
  }
}
