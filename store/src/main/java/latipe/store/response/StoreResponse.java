package latipe.store.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    String name,
    String description,
    String logo,
    String ownerId,
    String cover,
    @JsonProperty(value = "isDeleted")
    Boolean isDeleted
) {


}

