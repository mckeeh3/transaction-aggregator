package io.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/merchant")
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

  @PostMapping("/{merchantId}/create-merchant")
  public Effect<String> createMerchant(@RequestBody CreateMerchantCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), "createMerchant");
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/{merchantId}/update-day")
  public Effect<String> updateDay(@RequestBody UpdateDayCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), "updateDay");
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/{merchantId}/start-next-payment-cycle")
  public Effect<String> startNextPaymentCycle(@RequestBody StartNextPaymentCycleCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), "startNextPaymentCycle");
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
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
    public static State empty() {
      return new State(MerchantKey.empty(), 0);
    }

    public MerchantCreatedEvent eventsFor(CreateMerchantCommand command) {
      return new MerchantCreatedEvent(command.key());
    }

    public DayUpdatedEvent eventsFor(UpdateDayCommand command) {
      return new DayUpdatedEvent(toPaymentKey(currentPaymentId()), command.payload);
    }

    public NextPaymentCycleStartedEvent eventsFor(StartNextPaymentCycleCommand command) {
      return new NextPaymentCycleStartedEvent(toPaymentKey(currentPaymentId()), toPaymentKey(currentPaymentId() + 1));
    }

    public State on(MerchantCreatedEvent event) {
      return new State(event.key(), 1);
    }

    public State on(DayUpdatedEvent event) {
      return this;
    }

    public State on(NextPaymentCycleStartedEvent event) {
      return new State(key, currentPaymentId() + 1);
    }

    private PaymentKey toPaymentKey(int paymentId) {
      var paymentIdStr = "%s".formatted(paymentId);
      return new PaymentKey(paymentIdStr, key);
    }
  }

  public record CreateMerchantCommand(MerchantKey key) {}

  public record MerchantCreatedEvent(MerchantKey key) {}

  public record UpdateDayCommand(Payload payload) {}

  public record DayUpdatedEvent(PaymentKey key, Payload payload) {}

  public record StartNextPaymentCycleCommand(MerchantKey key) {}

  public record NextPaymentCycleStartedEvent(PaymentKey priorKey, PaymentKey nextKey) {}
}
