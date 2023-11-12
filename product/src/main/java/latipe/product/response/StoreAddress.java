package latipe.product.response;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreAddress {

  @Id
  private String id = new ObjectId().toString();
  @NotEmpty(message = "Contact name is required")
  private String contactName;
  @NotEmpty(message = "Phone name is required")
  private String phone;
  @NotEmpty(message = "Detail Address is required")
  private String detailAddress;
  private String zipCode;
  private String cityOrProvinceId;
  private String cityOrProvinceName;
  private String districtId;
  private String districtName;
  private String wardId;
  private String wardName;
  private Long countryId = 84L;
  private String countryName = "VietNam";

}
