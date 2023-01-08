package io.aggregator;

public record MerchantKey(
    String merchantId,
    String serviceCode,
    String accountFrom,
    String accountTo) {

  static MerchantKey empty() {
    return new MerchantKey("", "", "", "");
  }

  boolean isEmpty() {
    return merchantId.isEmpty() && serviceCode.isEmpty() && accountFrom.isEmpty() && accountTo.isEmpty();
  }
}
