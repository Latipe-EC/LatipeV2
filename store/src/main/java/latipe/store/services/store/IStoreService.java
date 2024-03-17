package latipe.store.services.store;


import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.store.constants.EStatusBan;
import latipe.store.dtos.PagedResultDto;
import latipe.store.request.BanStoreRequest;
import latipe.store.request.CheckBalanceRequest;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.MultipleStoreRequest;
import latipe.store.request.UpdateBalanceRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodeResponse;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreAdminResponse;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.product.ProductStoreResponse;

public interface IStoreService {

  CompletableFuture<StoreResponse> create(CreateStoreRequest input, HttpServletRequest request);

  CompletableFuture<StoreResponse> update(UpdateStoreRequest input
      , HttpServletRequest request)
      throws InvocationTargetException, IllegalAccessException;

  CompletableFuture<String> getStoreByUserId(String userId
      , HttpServletRequest request);

  CompletableFuture<StoreResponse> getDetailStoreById(String storeId
      , HttpServletRequest request);

  CompletableFuture<StoreDetailResponse> getMyStore(HttpServletRequest request);

  CompletableFuture<ProvinceCodesResponse> getProvinceCodes(GetProvinceCodesRequest input
      , HttpServletRequest request);

  CompletableFuture<ProvinceCodeResponse> getProvinceCode(String storeId
      , HttpServletRequest request);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip, int limit,
      String name, String orderBy
      , HttpServletRequest request);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStore(long skip, int limit,
      String name, String orderBy, String storeId
      , HttpServletRequest request);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip, int limit,
      String name, String orderBy
      , HttpServletRequest request);

  CompletableFuture<List<StoreSimplifyResponse>> getMultipleStore(MultipleStoreRequest input
      , HttpServletRequest request);

  CompletableFuture<Void> checkBalance(CheckBalanceRequest input
      , HttpServletRequest request);

  CompletableFuture<Void> UpdateBalance(UpdateBalanceRequest input
      , HttpServletRequest request);

  CompletableFuture<PagedResultDto<StoreAdminResponse>> getStoreAdmin(String keyword,
      Long skip,
      Integer size,
      String orderBy,
      EStatusBan isBan
      , HttpServletRequest request);

  CompletableFuture<Void> banStore(String storeId, BanStoreRequest input
      , HttpServletRequest request);

  CompletableFuture<StoreDetailResponse> getDetailStoreByAdmin(String userId
      , HttpServletRequest request);

  CompletableFuture<Long> countAllStore(
      HttpServletRequest request);
}
