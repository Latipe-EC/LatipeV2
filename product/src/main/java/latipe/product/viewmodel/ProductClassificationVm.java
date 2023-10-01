package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record ProductClassificationVm(
    String image,
    String name,
    int quantity,
    Double price,
    Double promotionalPrice,
    String sku,
    String code
) {

  public static ProductClassificationVm setCodeName(ProductClassificationVm classification,
      String code, String name) {
    return new ProductClassificationVm(classification.image, name
        , classification.quantity, classification.price, classification.promotionalPrice(),
        classification.sku, code);
  }
}
