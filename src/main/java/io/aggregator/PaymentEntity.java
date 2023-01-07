package io.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.springsdk.annotations.EntityKey;
import kalix.springsdk.annotations.EntityType;
import kalix.springsdk.annotations.EventHandler;

@EntityKey("paymentId")
@EntityType("Payment")
@RequestMapping("/payment")
public class PaymentEntity extends EventSourcedEntity<PaymentEntity.State> {
  private static final Logger log = LoggerFactory.getLogger(PaymentEntity.class);
  private final String entityId;

  public PaymentEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public State emptyState() {
    return State.empty();
  }

  @PostMapping("/{paymentId}/create-payment")
  public Effect<String> createPayment(@RequestBody CreatePaymentCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/{paymentId}/update-payment")
  public Effect<String> updatePayment(@RequestBody UpdatePaymentCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/{paymentId}/start-next-payment-cycle")
  public Effect<String> startNextPaymentCycle(@RequestBody StartNextPaymentCycleCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @GetMapping("/{paymentId}")
  public Effect<Payload> getPayment() {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), "getPayment");
    return effects().reply(currentState().payload());
  }

  @EventHandler
  public State handle(PaymentCreatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(PaymentUpdatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(NextPaymentCycleStartedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  public record State(
      PaymentKey key,
      List<Payload> plusDays,
      List<Payload> minusDays) {

    static State empty() {
      return new State(PaymentKey.empty(), List.of(), List.of());
    }

    Payload payload() {
      var initial = new Payload(key.toPayloadKey(), 0.0);
      return plusDays.stream().map(p -> minusDayPayload(p)).reduce(initial, (p1, p2) -> p1.add(p2));
    }

    private Payload minusDayPayload(Payload payload) {
      var minusDay = minusDays.stream().filter(p -> p.eqPayload(payload)).findFirst();

      return minusDay.isPresent()
          ? new Payload(key.toPayloadKey(), payload.amount() - minusDay.get().amount())
          : payload;
    }

    PaymentCreatedEvent eventsFor(CreatePaymentCommand command) {
      return new PaymentCreatedEvent(
          command.key(),
          command.priorPaymentDays());
    }

    PaymentUpdatedEvent eventsFor(UpdatePaymentCommand command) {
      return new PaymentUpdatedEvent(
          command.key(),
          command.payload());
    }

    NextPaymentCycleStartedEvent eventsFor(StartNextPaymentCycleCommand command) {
      var priorMinusDays = minusDays.stream().filter(m -> notPlusDay(m)).toList();
      var priorPaymentDays = new ArrayList<Payload>(priorMinusDays);
      priorPaymentDays.addAll(this.plusDays);
      return new NextPaymentCycleStartedEvent(
          command.key(),
          priorPaymentDays);
    }

    State on(PaymentCreatedEvent event) {
      return new State(
          event.key,
          this.plusDays,
          event.priorPaymentDays());
    }

    State on(PaymentUpdatedEvent event) {
      var filtered = plusDays.stream().filter(p -> !p.eqPayload(event.payload())).toList();
      var newPlusDays = new ArrayList<Payload>(filtered);
      newPlusDays.add(event.payload());
      return new State(key, newPlusDays, minusDays);
    }

    State on(NextPaymentCycleStartedEvent event) {
      return this;
    }

    private boolean notPlusDay(Payload payload) {
      return plusDays.stream().noneMatch(p -> p.eqPayload(payload));
    }
  }

  public record CreatePaymentCommand(PaymentKey key, List<Payload> priorPaymentDays) {}

  public record PaymentCreatedEvent(PaymentKey key, List<Payload> priorPaymentDays) {}

  public record UpdatePaymentCommand(PaymentKey key, Payload payload) {}

  public record PaymentUpdatedEvent(PaymentKey key, Payload payload) {}

  public record StartNextPaymentCycleCommand(PaymentKey key) {}

  public record NextPaymentCycleStartedEvent(PaymentKey key, List<Payload> priorPaymentDays) {}
}
