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
    String districtId,
    String districtName,
    String cityOrProvinceId,
    String cityOrProvinceName,
    String wardId,
    String wardName,
    Long countryId,
    String countryName
) {

}
