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

  static PayloadKey from(MerchantKey merchantKey, EpochTime epochTime) {
    return new PayloadKey(
        merchantKey.merchantId(),
        merchantKey.serviceCode(),
        merchantKey.accountFrom(),
        merchantKey.accountTo(),
        epochTime);
  }

  String entityId() {
    return "%s_%s_%s_%s_%s".formatted(
        merchantId, serviceCode, accountFrom, accountTo, epochTime.entityId());
  }
}
