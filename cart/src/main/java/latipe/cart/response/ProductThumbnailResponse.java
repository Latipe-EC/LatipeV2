package latipe.cart.response;

public record ProductThumbnailResponse(String id ,String optionId, String name, String nameOption, Double price,
                                       String thumbnailUrl, String storeId, String storeName,
                                       String cityOrProvinceId) {


}
