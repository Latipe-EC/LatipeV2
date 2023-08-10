package latipe.user.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class UserAddress {
    @Id
    private String id;
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
    private String countryName = "VietNam";
}
