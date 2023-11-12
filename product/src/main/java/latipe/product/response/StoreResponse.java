package latipe.product.response;

public record StoreResponse(String id, String name,
                            String description, String logo, String ownerId, String cover,
                            StoreAddress address
) {

}
