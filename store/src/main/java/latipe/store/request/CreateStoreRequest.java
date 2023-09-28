package latipe.store.request;

import jakarta.validation.constraints.NotEmpty;

public record CreateStoreRequest(
    @NotEmpty(message = "Name must not be empty")
    String name,
    @NotEmpty(message = "Name must not be empty")

    String description,
    String logo,
    String cover) {

}
