package latipe.store.response;


import java.util.List;
import latipe.store.entity.StoreAddress;

public record StoreDetailResponse(String id, String name,
                                  String description, String logo, String ownerId, String cover,
                                  StoreAddress address,
                                  Boolean isDeleted,
                                  Double feePerOrder,
                                  Double eWallet,
                                  List<Integer> ratings) {


}

