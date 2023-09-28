package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record ProductPriceVm(
    int quantity,
    double price,
    String code,
    String image) {

}
