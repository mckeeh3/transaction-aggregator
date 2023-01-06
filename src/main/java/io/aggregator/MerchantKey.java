package io.aggregator;

public record MerchantKey(String merchantId, String serviceCode, String accountFrom, String accountTo) {

  public static MerchantKey empty() {
    return new MerchantKey("", "", "", "");
  }
}
