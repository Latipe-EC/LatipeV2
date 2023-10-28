package latipe.user.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record CreateUserAddressRequest(@NotEmpty(message = "Contact name is required")
                                       String contactName,
                                       @NotEmpty(message = "Phone name is required")
                                       String phone,
                                       @NotEmpty(message = "Detail Address is required")
                                       String detailAddress,
                                       String zipCode,
                                       String cityOrProvinceId,
                                       String cityOrProvinceName,
                                       String districtId,
                                       String districtName,
                                       String wardId,
                                       String wardName,
                                       Long countryId,
                                       String countryName) {

}
