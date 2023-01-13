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

@EntityKey("merchantId")
@EntityType("Merchant")
@RequestMapping("/merchant/{merchantId}")
public class MerchantEntity extends EventSourcedEntity<MerchantEntity.State> {
  private static final Logger log = LoggerFactory.getLogger(MerchantEntity.class);
  private final String entityId;

  public MerchantEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public State emptyState() {
    return State.empty();
  }

  @PostMapping("/create-merchant")
  public Effect<String> createMerchant(@RequestBody CreateMerchantCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/update-day")
  public Effect<String> updateDay(@RequestBody UpdateDayCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvents(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/start-next-payment-cycle")
  public Effect<String> startNextPaymentCycle(@RequestBody StartNextPaymentCycleCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventFor(command))
        .thenReply(__ -> "OK");
  }

  @GetMapping
  public Effect<State> get() {
    log.info("EntityId: {}\nState: {}", entityId, currentState());
    return effects().reply(currentState());
  }

  @EventHandler
  public State handle(MerchantCreatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(DayUpdatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(NextPaymentCycleStartedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  public record State(MerchantKey key, int currentPaymentId) {
    static State empty() {
      return new State(MerchantKey.empty(), 0);
    }

    MerchantCreatedEvent eventFor(CreateMerchantCommand command) {
      return new MerchantCreatedEvent(command.key());
    }

    List<?> eventsFor(UpdateDayCommand command) {
      var events = new ArrayList<>();
      if (key.isEmpty()) {
        events.add(new MerchantCreatedEvent(command.key()));
      }
      events.add(new DayUpdatedEvent(toPaymentKey(currentPaymentId()), command.payload));

      return events;
    }

    NextPaymentCycleStartedEvent eventFor(StartNextPaymentCycleCommand command) {
      return new NextPaymentCycleStartedEvent(toPaymentKey(currentPaymentId()), toPaymentKey(currentPaymentId() + 1));
    }

    State on(MerchantCreatedEvent event) {
      return new State(event.key(), 1);
    }

    State on(DayUpdatedEvent event) {
      return this;
    }

    State on(NextPaymentCycleStartedEvent event) {
      return new State(key, currentPaymentId() + 1);
    }

    private PaymentKey toPaymentKey(int paymentId) {
      var paymentIdStr = "%s".formatted(paymentId);
      return new PaymentKey(paymentIdStr, key);
    }
  }

  public record CreateMerchantCommand(MerchantKey key) {}

  public record MerchantCreatedEvent(MerchantKey key) {}

  public record UpdateDayCommand(MerchantKey key, Payload payload) {}

  public record DayUpdatedEvent(PaymentKey key, Payload payload) {}

  public record StartNextPaymentCycleCommand(MerchantKey key) {}

  public record NextPaymentCycleStartedEvent(PaymentKey priorKey, PaymentKey nextKey) {}
}
