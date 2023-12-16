package latipe.user.services.user;

import java.util.concurrent.CompletableFuture;
import latipe.user.constants.EStatusBan;
import latipe.user.dtos.PagedResultDto;
import latipe.user.entity.UserAddress;
import latipe.user.request.BanUserRequest;
import latipe.user.request.CancelOrderRequest;
import latipe.user.request.CheckBalanceRequest;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserNameRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.InfoRatingResponse;
import latipe.user.response.UserAdminResponse;
import latipe.user.response.UserResponse;

public interface IUserService {

  CompletableFuture<UserResponse> create(CreateUserRequest input);

  CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(String id, int page, int size);

  CompletableFuture<UserAddress> addMyAddresses(String id,
      CreateUserAddressRequest input);

  CompletableFuture<Void> deleteMyAddresses(String id, String userId);

  CompletableFuture<UserAddress> getMyAddresses(String id, String userId);

  CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
      String userId, String addressId);

  CompletableFuture<UserResponse> register(RegisterRequest input);

  CompletableFuture<UserResponse> updateProfile(String id, UpdateUserRequest input);

  CompletableFuture<UserResponse> getProfile(String id);

  CompletableFuture<Long> countAllUser();

  CompletableFuture<Void> upgradeVendor(String userId);

  CompletableFuture<Integer> countMyAddress(String userId);

  CompletableFuture<Void> checkBalance(CheckBalanceRequest request);

  CompletableFuture<Void> cancelOrder(CancelOrderRequest request);

  CompletableFuture<Void> updateUserName(UpdateUserNameRequest request, String userId);

  CompletableFuture<InfoRatingResponse> getInfoForRating(String userId);

  CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(String keyword,
      Long skip,
      Integer size,
      String orderBy,
      EStatusBan isBan);

  CompletableFuture<Void> banUser(String userId, BanUserRequest request);

}
