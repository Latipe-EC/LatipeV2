package latipe.product.viewmodel;

import java.util.List;

public record ProductMessageVm(String id, String op, Boolean isBanned, List<String> images) {

}
