package io.aggregator;

import java.util.List;

public record Payment(String merchantId, String paymentId, List<Payload> plusDays, List<Payload> minusDays) {
  public static Payment empty() {
    return new Payment("", "", List.of(), List.of());
  }
}
