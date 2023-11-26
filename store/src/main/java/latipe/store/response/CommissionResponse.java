package latipe.store.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record CommissionResponse(
    @JsonProperty(value = "id", required = true) String id,
    String name,
    Double feeOrder,
    Integer minPoint
) {


}

