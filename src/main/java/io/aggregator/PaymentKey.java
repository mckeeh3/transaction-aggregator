package io.aggregator;

public record PaymentKey(String paymentId, MerchantKey merchantKey) {

  static PaymentKey empty() {
    return new PaymentKey("", MerchantKey.empty());
  }

  PayloadKey toPayloadKey() {
    return new PayloadKey(
        merchantKey.merchantId(),
        merchantKey.serviceCode(),
        merchantKey.accountFrom(),
        merchantKey.accountTo(),
        EpochTime.now().toDay());
  }

  String entityId() {
    return "%s_%s".formatted(paymentId, merchantKey.entityId());
  }
}
