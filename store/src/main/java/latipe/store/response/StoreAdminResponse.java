package latipe.store.response;


import java.util.List;

public record StoreAdminResponse(
    String id, String name,
    Integer point, Double eWallet,
    Boolean isDeleted,
    Boolean isBan,
    String reasonBan,
    List<Integer> ratings
) {

  public static StoreAdminResponse setId(StoreAdminResponse storeAdminResponse,
      String id) {
    return new StoreAdminResponse(
        id,
        storeAdminResponse.name(),
        storeAdminResponse.point(),
        storeAdminResponse.eWallet(),
        storeAdminResponse.isDeleted(),
        storeAdminResponse.isBan(),
        storeAdminResponse.reasonBan(),
        storeAdminResponse.ratings()
    );
  }


}

