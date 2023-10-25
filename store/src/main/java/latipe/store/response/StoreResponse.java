package latipe.store.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import latipe.store.Entity.StoreAddress;

public record StoreResponse(@JsonProperty(value = "id", required = true) String id, String name,
                            String description, String logo, String ownerId, String cover,
                            StoreAddress address,
                            @JsonProperty(value = "isDeleted") Boolean isDeleted) {


}

