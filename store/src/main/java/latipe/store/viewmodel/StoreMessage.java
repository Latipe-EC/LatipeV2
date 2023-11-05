package latipe.store.viewmodel;

import lombok.Builder;

@Builder
public record StoreMessage(String id, String op) {

}
