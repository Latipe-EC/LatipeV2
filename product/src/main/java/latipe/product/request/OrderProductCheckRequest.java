package latipe.product.request;

public record OrderProductCheckRequest(
    String productId,
    String optionId,
    int quantity) {

}
