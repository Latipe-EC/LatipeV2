package latipe.store.request;

import latipe.store.entity.StoreAddress;

public record UpdateStoreRequest(
    String name,
    String description,
    String logo,
    String cover,
    StoreAddress address
) {

}
