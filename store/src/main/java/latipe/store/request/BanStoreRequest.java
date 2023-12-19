package latipe.store.request;


import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BanStoreRequest(
    Boolean isBanned,
    @Size(min = 5)
    String reason
) {


}