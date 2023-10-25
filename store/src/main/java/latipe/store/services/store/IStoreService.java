package latipe.store.services.store;


import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface IStoreService {

  CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input, String token);

  CompletableFuture<StoreResponse> update(String userId, String storeId, UpdateStoreRequest input)
      throws InvocationTargetException, IllegalAccessException;

  CompletableFuture<String> getStoreByUserId(String userId);

  CompletableFuture<ProvinceCodesResponse> getProvinceCodes(
      @RequestBody GetProvinceCodesRequest input);

}
