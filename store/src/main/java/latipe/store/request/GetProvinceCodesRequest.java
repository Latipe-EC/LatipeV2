package latipe.store.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.store.Entity.StoreAddress;

public record GetProvinceCodesRequest(
    List<String> ids) {

}
