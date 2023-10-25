package latipe.store.request;

import latipe.store.Entity.StoreAddress;

public record UpdateStoreRequest(
    String name,
    String description,
    String logo,
    String cover,
    StoreAddress address
) {

}
