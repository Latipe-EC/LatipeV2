package latipe.cart.response;


import latipe.cart.Entity.Cart;
import lombok.Builder;

@Builder
public record CartGetDetailResponse(
    String id, String userId, String productId,
    String productOptionId, int quantity, String productName,
    String storeId, String storeName, String image,
    String nameOption,
    Double price
) {

  public static CartGetDetailResponse fromModel(Cart cart, ProductThumbnailResponse product) {
    return new CartGetDetailResponse(cart.getId(), cart.getUserId(), cart.getProductId(),
        cart.getProductOptionId(), cart.getQuantity(), product.name(), product.storeId(),
        product.storeName(), product.thumbnailUrl(), product.nameOption(), product.price());
  }


}
