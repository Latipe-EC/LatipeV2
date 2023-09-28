package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record ProductOrderVm(
    String productId,
    String name,
    int quantity,
    double price,
    String optionId,
    String nameOption,
    Double totalPrice) {

}
