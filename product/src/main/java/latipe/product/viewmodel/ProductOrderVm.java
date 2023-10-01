package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record ProductOrderVm(
    String productId,
    String name,
    int quantity,
    double price,
    double promotionalPrice,
    String optionId,
    String nameOption,
    String storeId,
    Double totalPrice) {

}
