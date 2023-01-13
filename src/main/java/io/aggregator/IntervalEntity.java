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
import kalix.springsdk.annotations.EventHandler;

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
    log.info("EntityId: {}\nState: {}\nCommand: {}", entityId, currentState(), command);
    return effects()
        .emitEvent(currentState().eventsFor(command))
        .thenReply(__ -> "OK");
  }

  @EventHandler
  public State handle(SubIntervalUpdatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(IntervalUpdatedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  @EventHandler
  public State handle(CurrentStateReleasedEvent event) {
    log.info("EntityId: {}\nState: {}\nEvent: {}", entityId, currentState(), event);
    return currentState().on(event);
  }

  public record State(
      IntervalKey key,
      Payload payload,
      List<State> subIntervals,
      boolean hasChanged) {

    static State empty() {
      return new State(IntervalKey.empty(), Payload.empty(), List.of(), false);
    }

    List<?> eventsFor(UpdateSubIntervalCommand command) {
      var events = new ArrayList<>();

      if (!hasChanged) {
        var subIntervals = updateSubIntervals(command.subInterval);
        var newState = updateInterval(command.key, subIntervals);
        events.add(new IntervalUpdatedEvent(newState.cloneWithoutSubIntervals(command.key, true)));
      }

      events.add(new SubIntervalUpdatedEvent(command.key, command.subInterval));

      return events;
    }

    CurrentStateReleasedEvent eventsFor(ReleaseCurrentStateCommand command) {
      return new CurrentStateReleasedEvent(cloneWithoutSubIntervals(command.key, false));
    }

    public State on(SubIntervalUpdatedEvent event) {
      var updatedSubIntervals = updateSubIntervals(event.subInterval);
      return updateInterval(event.key, updatedSubIntervals);
    }

    public State on(IntervalUpdatedEvent event) {
      return updateInterval(event.interval.key, subIntervals);
    }

    public State on(CurrentStateReleasedEvent event) {
      if (EpochTime.Level.millisecond.equals(key.epochTime().level())) {
        log.info("MILLISECOND {}", key);
      }
      return new State(event.interval.key, payload, subIntervals, false);
    }

    private List<State> updateSubIntervals(State subInterval) {
      if (subIntervals.isEmpty()) {
        return List.of(subInterval);
      }
      var updatedSubIntervals = new ArrayList<State>(subIntervals.stream().filter(s -> !eqInterval(s, subInterval)).toList());
      updatedSubIntervals.add(subInterval);
      return updatedSubIntervals;
    }

    private State updateInterval(IntervalKey key, List<State> subIntervals) {
      var initial = new Payload(key.toPayloadKey(), 0.0);
      var payload = subIntervals.stream().map(s -> s.payload).reduce(initial, (total, p) -> total.add(p));
      if (EpochTime.Level.millisecond.equals(key.epochTime().level())) {
        log.info("MILLISECOND {}", key);
      }
      return new State(key, payload, subIntervals, true);
    }

    private State cloneWithoutSubIntervals(IntervalKey key, boolean hasChanged) {
      if (EpochTime.Level.millisecond.equals(key.epochTime().level())) {
        log.info("MILLISECOND {}", key);
      }
      return new State(key, this.payload, List.of(), hasChanged);
    }

    private boolean eqInterval(State s1, State s2) {
      return s1.key.equals(s2.key);
    }
  }

  public record UpdateSubIntervalCommand(IntervalKey key, IntervalEntity.State subInterval) {}

  public record ReleaseCurrentStateCommand(IntervalKey key) {}

  public record CurrentStateReleasedEvent(IntervalEntity.State interval) {}

  public record SubIntervalUpdatedEvent(IntervalKey key, IntervalEntity.State subInterval) {}

  public record IntervalUpdatedEvent(IntervalEntity.State interval) {}
}
