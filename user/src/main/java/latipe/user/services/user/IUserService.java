package latipe.user.services.user;

import latipe.user.Entity.UserAddress;
import latipe.user.services.IService;
import latipe.user.services.user.Dtos.CreateUserAddressDto;
import latipe.user.services.user.Dtos.RegisterDto;
import latipe.user.services.user.Dtos.UserCreateDto;
import latipe.user.services.user.Dtos.UserDto;
import latipe.user.services.user.Dtos.UserUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.user.viewmodel.RegisterRequest;

public interface IUserService extends IService<UserDto, UserCreateDto, UserUpdateDto> {
    public CompletableFuture<List<UserAddress>> getMyAddresses(String id, int page, int size);
    public CompletableFuture<UserAddress> addMyAddresses(String id, CreateUserAddressDto input);
    public CompletableFuture<Void> deleteMyAddresses(String id);
    public CompletableFuture<UserAddress> updateMyAddresses(UserAddress input, String userId, String addressId) ;
    public CompletableFuture<UserDto> register(RegisterRequest input);
}
