package latipe.store.request;

public record UpdateStoreRequest(
    String name,
    String description,
    String logo,
    String cover
) {

}
