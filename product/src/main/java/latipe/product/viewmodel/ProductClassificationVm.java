package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record ProductClassificationVm(
    String image,
    String name,
    int quantity,
    double price,
    String sku,
    String code
) {

  public static ProductClassificationVm setCode(ProductClassificationVm classification,
      String code) {
    return new ProductClassificationVm(classification.image, classification.name
        , classification.quantity, classification.price, classification.sku, code);
  }

  public static ProductClassificationVm setCodeName(ProductClassificationVm classification,
      String code, String name) {
    return new ProductClassificationVm(classification.image, name
        , classification.quantity, classification.price, classification.sku, code);
  }
}
