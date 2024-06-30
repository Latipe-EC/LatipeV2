package latipe.payment.constants;

public class CONSTANTS {

    public static final String REQUEST_ID = "req-id";
    public static final String PAYPAL_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYPAL_STATUS_APPROVED = "APPROVED";
    public static final int ORDER_SYSTEM_PROCESS = 0;
    public static final int ORDER_CREATED = 1;
    public static final int ORDER_PREPARED = 2;
    public static final int ORDER_DELIVERY = 3;
    public static final int ORDER_SHIPPING_FINISH = 4;
    public static final int ORDER_COMPLETED = 5;
    public static final int ORDER_REFUND = 6;
    public static final int ORDER_CANCEL_BY_USER = -2;
    public static final int ORDER_CANCEL_BY_STORE = -3;
    public static final int ORDER_CANCEL_BY_ADMIN = -4;
    public static final int ORDER_CANCEL_BY_DELI = -5;
    public static final int ORDER_CANCEL_USER_REJECT = -7;
    public static final int ORDER_FAILED = -1;

    public static final String PAYPAL_CONFIG_CODE = "PaypalPayment";
}
