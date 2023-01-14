package io.aggregator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;

//
// TODO - this is a temporary solution to get the demo working
//

@RequestMapping("/http-transaction-event-to-interval")
public class HttpTransactionEventToIntervalAction extends Action {
  private static final Logger log = LoggerFactory.getLogger(HttpTransactionEventToIntervalAction.class);
  private final KalixClient kalixClient;

  public HttpTransactionEventToIntervalAction(KalixClient kalixClient) {
    this.kalixClient = kalixClient;
  }

  @PutMapping("/json-update-sub-interval")
  public Effect<String> updateSubInterval(@RequestBody UpdateSubIntervalCommand commandIn) {
    log.info("Command: {}", commandIn);

    var subIntervalKey = commandIn.key();
    var intervalKey = subIntervalKey.toLevelUp();
    var path = "/interval/%s/update-sub-interval".formatted(intervalKey.toLevelUp().entityId());
    var commandOut = new IntervalEntity.UpdateSubIntervalCommand(
        intervalKey,
        new IntervalEntity.State(commandIn.key, commandIn.payload, List.of(), false));
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, commandOut, returnType);

    return effects().forward(deferredCall);
  }

  @PutMapping("/simple-update-sub-interval")
  public Effect<String> singleTransactionEvent(@RequestBody SingleTransactionEventCommand commandIn) {
    log.info("Command: {}", commandIn);

    var epochTime = EpochTime.now().toTransaction();
    var merchantKey = new MerchantKey("merchant-1", "service-code-1", "account-from-1", "account-to-1");
    var subIntervalKey = new IntervalKey(merchantKey, commandIn.transactionId, epochTime);
    var intervalKey = subIntervalKey.toLevelUp();
    var payloadKey = PayloadKey.from(merchantKey, epochTime);
    var payload = new Payload(payloadKey, commandIn.amount);
    var path = "/interval/%s/update-sub-interval".formatted(subIntervalKey.toLevelUp().entityId());
    var commandOut = new IntervalEntity.UpdateSubIntervalCommand(
        intervalKey,
        new IntervalEntity.State(subIntervalKey, payload, List.of(), false));
    var returnType = String.class;
    var deferredCall = kalixClient.put(path, commandOut, returnType);

    return effects().forward(deferredCall);
  }

  @GetMapping("/release-current-state")
  public Effect<UpdateSubIntervalCommand> releaseCurrentState() {
    log.info("Command: releaseCurrentState");

    return effects().reply(new UpdateSubIntervalCommand(IntervalKey.empty(), Payload.empty()));
  }

  public record UpdateSubIntervalCommand(IntervalKey key, Payload payload) {}

  public record SingleTransactionEventCommand(String transactionId, double amount) {}
}
