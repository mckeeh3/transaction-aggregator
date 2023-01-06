package io.aggregator;

public record PayloadKey(
    String merchantId,
    String serviceCode,
    String accountFrom,
    String accountTo,
    EpochTime epochTime) {

  static PayloadKey empty() {
    return new PayloadKey("", "", "", "", EpochTime.empty());
  }

  String entityId() {
    return "%s_%s_%s_%s_%s".formatted(
        merchantId, serviceCode, accountFrom, accountTo, epochTime.entityId());
  }
}
