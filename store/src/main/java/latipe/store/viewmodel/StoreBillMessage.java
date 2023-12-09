package latipe.store.viewmodel;

public record StoreBillMessage(
    String storeId,
    String orderUuid,
    Integer amountReceived,
    Integer systemFee
) {

}
