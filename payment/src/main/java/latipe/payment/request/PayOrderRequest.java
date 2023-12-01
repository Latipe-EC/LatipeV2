package latipe.payment.request;


import lombok.Builder;

@Builder
public record PayOrderRequest(
    String orderId
) {


}