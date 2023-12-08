package latipe.store.services.store;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.store.dtos.PagedResultDto;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.MultipleStoreRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.product.ProductStoreResponse;

public interface IStoreService {

  CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input, String token);

  CompletableFuture<StoreResponse> update(String userId, UpdateStoreRequest input)
      throws InvocationTargetException, IllegalAccessException;

  CompletableFuture<String> getStoreByUserId(String userId);

  CompletableFuture<StoreResponse> getDetailStoreById(String storeId);

  CompletableFuture<StoreDetailResponse> getMyStore(String userId);

  CompletableFuture<ProvinceCodesResponse> getProvinceCodes(GetProvinceCodesRequest input);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip, int limit,
      String name, String orderBy, String userId);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStore(long skip, int limit,
      String name, String orderBy, String storeId);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip, int limit,
      String name, String orderBy, String userId);

  CompletableFuture<List<StoreSimplifyResponse>> getMultipleStore(MultipleStoreRequest input);
}
