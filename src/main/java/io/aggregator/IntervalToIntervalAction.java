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
    var path = "/interval/%s/release-current-state".formatted(event.interval().key());
    var command = new IntervalEntity.ReleaseCurrentStateCommand(event.interval().key());
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, command, returnType);

    return effects().forward(deferredCall);
  }
}
