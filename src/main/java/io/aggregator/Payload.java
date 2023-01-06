package io.aggregator;

record Payload(PayloadKey key, double amount) {
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
