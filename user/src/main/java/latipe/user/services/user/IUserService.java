package latipe.user.services.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.user.Entity.UserAddress;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.UserResponse;

public interface IUserService {

  public CompletableFuture<UserResponse> create(CreateUserRequest input);

  public CompletableFuture<List<UserAddress>> getMyAddresses(String id, int page, int size);

  public CompletableFuture<UserAddress> addMyAddresses(String id,
      CreateUserAddressRequest input);

  public CompletableFuture<Void> deleteMyAddresses(String id, String userId);

  public CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
      String userId, String addressId);

  public CompletableFuture<UserResponse> register(RegisterRequest input);

  public CompletableFuture<UserResponse> updateProfile(String id, UpdateUserRequest input);

  public CompletableFuture<UserResponse> getProfile(String id);

}
