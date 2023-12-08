package latipe.store.response;


import latipe.store.entity.StoreAddress;

public record StoreDetailResponse(String id, String name,
                                  String description, String logo, String ownerId, String cover,
                                  StoreAddress address,
                                  Boolean isDeleted,
                                  Double feePerOrder,
                                  Double eWallet,
                                  Double rating) {


}

