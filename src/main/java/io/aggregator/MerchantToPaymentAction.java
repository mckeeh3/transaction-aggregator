package io.aggregator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

@Subscribe.EventSourcedEntity(value = MerchantEntity.class, ignoreUnknown = true)
public class MerchantToPaymentAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(MerchantToPaymentAction.class);
  private final KalixClient kalixClient;

  public MerchantToPaymentAction(KalixClient kalixClient) {
    this.kalixClient = kalixClient;
  }

  public Effect<String> on(MerchantEntity.MerchantCreatedEvent event) {
    log.info("Event: {}", event);
    var path = "/payment/%s/create".formatted(event.paymentKey().entityId());
    var command = new PaymentEntity.CreatePaymentCommand(event.paymentKey(), List.of());
    var returnType = String.class;
    var deferredCall = kalixClient.post(path, command, returnType);

    return effects().forward(deferredCall);
  }

  public Effect<String> on(MerchantEntity.DayUpdatedEvent event) {
    log.info("Event: {}", event);
    var path = "/payment/%s/update".formatted(event.key().entityId());
    var command = new PaymentEntity.UpdatePaymentCommand(event.key(), event.payload());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }

  public Effect<String> on(MerchantEntity.NextPaymentCycleStartedEvent event) {
    log.info("Event: {}", event);
    var path = "/payment/%s/start-next-payment-cycle".formatted(event.priorKey().entityId());
    var command = new PaymentEntity.StartNextPaymentCycleCommand(event.priorKey(), event.nextKey());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }
}
