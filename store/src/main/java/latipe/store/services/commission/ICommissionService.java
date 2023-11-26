package latipe.store.services.commission;


import java.util.concurrent.CompletableFuture;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;

public interface ICommissionService {

  CompletableFuture<CommissionResponse> create(CreateCommissionRequest request);

  CompletableFuture<Void> delete(String commissionId);

  CompletableFuture<CommissionResponse> update(String commissionId,
      UpdateCommissionRequest request);

  Double calcPercentStore(String storeId);

}
