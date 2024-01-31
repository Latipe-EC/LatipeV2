package latipe.payment.request;

public record PayByPaypalRequest(String orderId, String id, String status, String email) {

}
