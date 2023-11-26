package latipe.store.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateCommissionRequest(
    @NotBlank(message = "Name must not be blank")
    String name,
    @Min(0)
    @Max(1)
    Double feeOrder,
    @Min(1)
    Integer minPoint
) {

}
