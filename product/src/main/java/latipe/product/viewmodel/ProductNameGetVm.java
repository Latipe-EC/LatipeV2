package latipe.product.viewmodel;


import latipe.product.entity.Product;

public record ProductNameGetVm(String name) {

  public static ProductNameGetVm fromModel(Product product) {
    return new ProductNameGetVm(
        product.getName()
    );
  }
}
