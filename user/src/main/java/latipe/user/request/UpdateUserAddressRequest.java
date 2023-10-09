package latipe.user.request;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserAddressRequest(
    @NotEmpty(message = "Contact name is required")
    String contactName,
    @NotEmpty(message = "Phone name is required")
    String phone,
    @NotEmpty(message = "Detail Address is required")
    String detailAddress,
    String zipCode,
    Long districtId,
    String districtName,
    Long cityOrProvinceId,
    String cityOrProvinceName,
    Long countryId,
    String countryName
) {

}
