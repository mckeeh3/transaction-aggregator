package io.aggregator;

public record IntervalKey(MerchantKey merchantKey, String transactionId, EpochTime epochTime) {

  static IntervalKey empty() {
    return new IntervalKey(MerchantKey.empty(), "", EpochTime.empty());
  }

  boolean isEmpty() {
    return merchantKey.isEmpty() && transactionId.isEmpty() && epochTime.isEmpty();
  }

  String entityId() {
    return "%s_%s_%s".formatted(merchantKey.entityId(), transactionId, epochTime.entityId());
  }

  PayloadKey toPayloadKey() {
    return new PayloadKey(
        merchantKey.merchantId(),
        merchantKey.serviceCode(),
        merchantKey.accountFrom(),
        merchantKey.accountTo(),
        epochTime());
  }
}
