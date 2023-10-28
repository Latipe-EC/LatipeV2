package latipe.store.response;

import java.util.List;
import lombok.Builder;

@Builder
public record ProvinceCodesResponse (List<String> codes) {

}
