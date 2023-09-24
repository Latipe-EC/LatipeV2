package latipe.user.request;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserAddressRequest(
    @NotEmpty(message = "Contact name is required")
     String contactName,
    @NotEmpty(message = "Phone name is required")
     String phone,
    @NotEmpty(message = "Detail Address is required")
     String detailAddress,
     String city,
     String zipCode,
     Long districtId,
     String districtName,
     Long stateOrProvinceId,
     String stateOrProvinceName,
     Long countryId,
     String countryName
) {

}
