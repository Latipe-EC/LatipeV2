package latipe.store.Entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
  private Long cityOrProvinceId;
  private String cityOrProvinceName;
  private Long districtId;
  private String districtName;
  private Long wardId;
  private String wardName;
  private Long countryId = 84L;
  private String countryName = "VietNam";

}
