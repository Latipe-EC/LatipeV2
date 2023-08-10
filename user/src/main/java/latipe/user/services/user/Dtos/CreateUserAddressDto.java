package latipe.user.services.user.Dtos;

import lombok.Data;

@Data
public class CreateUserAddressDto {
    private String contactName;
    private String phone;
    private String detailAddress;
    private String city;
    private String zipCode;
    private Long districtId;
    private String districtName;
    private Long stateOrProvinceId;
    private String stateOrProvinceName;
    private Long countryId = 84L;
    private String countryName;
}
