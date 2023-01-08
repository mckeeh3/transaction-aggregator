package io.aggregator;

record Payload(PayloadKey key, double amount) {
  static Payload empty() {
    return new Payload(PayloadKey.empty(), 0.0);
  }

  boolean eqPayload(Payload p) {
    return key.equals(p.key);
  }

  Payload zeroAmount() {
    return new Payload(key, 0.0);
  }

  Payload add(Payload p) {
    return new Payload(key, amount + p.amount);
  }
}
