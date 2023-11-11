package latipe.rating.response;

public record RatingResponse(String id, String content, Integer rating, String userId,
                             String userName, String productId, String storeId, String detail
 ) {

}
