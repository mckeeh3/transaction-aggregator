package io.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.springsdk.annotations.EntityKey;
import kalix.springsdk.annotations.EntityType;

@EntityKey("intervalId")
@EntityType("Interval")
@RequestMapping("/interval")
public class IntervalEntity extends EventSourcedEntity<IntervalEntity.State> {
  private static final Logger log = LoggerFactory.getLogger(IntervalEntity.class);
  private final String entityId;

  public IntervalEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public State emptyState() {
    return State.empty();
  }

  @PutMapping("/{intervalId}/update-sub-interval")
  public Effect<String> updateSubInterval(@RequestBody UpdateSubIntervalCommand command) {
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvents(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @PutMapping("/{intervalId}/release-current-state")
  public Effect<String> releaseCurrentState(@RequestBody ReleaseCurrentStateCommand command) {
    log.info("EntityId: {}\nState: {}\nReleaseCurrentStateCommand {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  public record State( //
      String merchantId,
      EpochTime epochTime,
      Payload payload,
      List<State> subIntervals,
      boolean hasChanged) {

    static State empty() {
      return new State("", EpochTime.empty(), Payload.empty(), List.of(), false);
    }

    public List<?> eventsFor(UpdateSubIntervalCommand command) {
      var events = new ArrayList<>();
      events.add(new SubIntervalUpdatedEvent(command.subInterval()));
      if (!hasChanged) {
        var subIntervals = updateSubIntervals(this.subIntervals, command.subInterval());
        var newState = updateInterval(this, subIntervals);
        events.add(new IntervalUpdatedEvent(newState.cloneWithoutSubIntervals(true)));
      }
      return events;
    }

    public CurrentStateReleasedEvent eventsFor(ReleaseCurrentStateCommand command) {
      return new CurrentStateReleasedEvent(cloneWithoutSubIntervals(false));
    }

    public List<State> updateSubIntervals(List<State> subIntervals, State subInterval) {
      if (subIntervals.isEmpty()) {
        return List.of(subInterval);
      }
      var updatedSubIntervals = new ArrayList<State>(subIntervals.stream().filter(s -> !eqInterval(s, subInterval)).toList());
      updatedSubIntervals.add(subInterval);
      return updatedSubIntervals;
    }

    private State updateInterval(State state, List<State> subIntervals) {
      var payload = subIntervals.stream().map(s -> s.payload).reduce(Payload.empty(), Payload::add);
      return new State(this.merchantId, this.epochTime, payload, this.subIntervals, true);
    }

    private State cloneWithoutSubIntervals(boolean hasChanged) {
      return new State(this.merchantId, this.epochTime, this.payload, List.of(), hasChanged);
    }

    private boolean eqInterval(State s1, State s2) {
      return s1.merchantId.equals(s2.merchantId) && s1.epochTime.equals(s2.epochTime);
    }
  }

  public record UpdateSubIntervalCommand(IntervalEntity.State subInterval) {}

  public record ReleaseCurrentStateCommand(String merchantId, EpochTime epochTime) {}

  public record CurrentStateReleasedEvent(IntervalEntity.State interval) {}

  public record SubIntervalUpdatedEvent(IntervalEntity.State subInterval) {}

  public record IntervalUpdatedEvent(IntervalEntity.State interval) {}

  public record Payload(String paymentId, double amount) {
    static Payload empty() {
      return new Payload("", 0);
    }

    Payload add(Payload other) {
      return new Payload(paymentId, amount + other.amount);
    }
  }
}
