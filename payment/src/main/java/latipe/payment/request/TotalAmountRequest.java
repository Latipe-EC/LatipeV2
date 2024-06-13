package latipe.payment.request;


import java.util.List;
import lombok.Builder;

@Builder
public record TotalAmountRequest(
    List<String> orderIds
) {


}