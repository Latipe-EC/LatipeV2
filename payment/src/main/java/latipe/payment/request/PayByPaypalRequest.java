package latipe.payment.request;

import java.util.List;

public record PayByPaypalRequest(List<String> orderIds, String id, String status, String email) {

}
