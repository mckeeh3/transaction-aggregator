package io.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

@Subscribe.EventSourcedEntity(value = IntervalEntity.class, ignoreUnknown = true)
public class IntervalToIntervalAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(IntervalToIntervalAction.class);
  private final KalixClient kalixClient;

  public IntervalToIntervalAction(KalixClient kalixClient) {
    this.kalixClient = kalixClient;
  }

  public Effect<String> on(IntervalEntity.IntervalUpdatedEvent event) {
    log.info("Event: {}", event);
    var path = "/interval/%s/release-current-state".formatted(event.interval().key().entityId());
    var command = new IntervalEntity.ReleaseCurrentStateCommand(event.interval().key());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }

  public Effect<String> on(IntervalEntity.CurrentStateReleasedEvent event) {
    log.info("Event: {}", event);
    if (event.interval().key().epochTime().level().equals(EpochTime.Level.day)) {
      return callMerchantInterval(event);
    } else {
      return callParentInterval(event);
    }
  }

  private Effect<String> callParentInterval(IntervalEntity.CurrentStateReleasedEvent event) {
    var parentIntervalKey = event.interval().key().toLevelUp();
    var path = "/interval/%s/update-sub-interval".formatted(parentIntervalKey.entityId());
    var command = new IntervalEntity.UpdateSubIntervalCommand(parentIntervalKey, event.interval());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }

  private Effect<String> callMerchantInterval(IntervalEntity.CurrentStateReleasedEvent event) {
    var merchantKey = event.interval().key().merchantKey();
    var path = "/merchant/%s/update-day".formatted(merchantKey.entityId());
    var command = new MerchantEntity.UpdateDayCommand(merchantKey, event.interval().payload());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }
}
