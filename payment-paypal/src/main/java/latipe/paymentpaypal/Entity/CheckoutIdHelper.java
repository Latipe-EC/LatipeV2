package latipe.paymentpaypal.Entity;

import lombok.Getter;

public class CheckoutIdHelper {

  @Getter
  private static String checkoutId;

  private CheckoutIdHelper() {
    // Private constructor to prevent instantiation
  }

  public static void setCheckoutId(String checkoutId) {
    CheckoutIdHelper.checkoutId = checkoutId;
  }
}
