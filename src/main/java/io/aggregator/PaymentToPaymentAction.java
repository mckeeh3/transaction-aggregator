package io.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

@Subscribe.EventSourcedEntity(value = PaymentEntity.class, ignoreUnknown = true)
public class PaymentToPaymentAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(PaymentToPaymentAction.class);
  private final KalixClient kalixClient;

  public PaymentToPaymentAction(KalixClient kalixClient) {
    this.kalixClient = kalixClient;
  }

  public Effect<String> on(PaymentEntity.NextPaymentCycleStartedEvent event) {
    log.info("Event: {}", event);
    var path = "/payment/%s/create".formatted(event.nextKey().entityId());
    var command = new PaymentEntity.CreatePaymentCommand(event.priorKey(), event.priorPaymentDays());
    var returnType = String.class;
    var deferredCall = kalixClient.post(path, command, returnType);

    return effects().forward(deferredCall);
  }
}
