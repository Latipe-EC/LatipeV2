package latipe.user.services.user;

import java.util.concurrent.CompletableFuture;
import latipe.user.dtos.PagedResultDto;
import latipe.user.entity.UserAddress;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserRequest;
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

  CompletableFuture<Void> upgradeVendor(String userId);

  CompletableFuture<Integer> countMyAddress(String userId);
}
