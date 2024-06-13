package latipe.user.services.user;

import jakarta.servlet.http.HttpServletRequest;
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

    CompletableFuture<UserResponse> create(CreateUserRequest input, HttpServletRequest request);

    CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(int page, int size,
        HttpServletRequest request);

    CompletableFuture<UserAddress> addMyAddresses(
        CreateUserAddressRequest input, HttpServletRequest request);

    CompletableFuture<Void> deleteMyAddresses(String id, HttpServletRequest request);

    CompletableFuture<UserAddress> getMyAddresses(String id, HttpServletRequest request);

    CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
        String addressId, HttpServletRequest request);

    CompletableFuture<UserResponse> register(RegisterRequest input, HttpServletRequest request);

    CompletableFuture<UserResponse> updateProfile(UpdateUserRequest input,
        HttpServletRequest request);

    CompletableFuture<UserResponse> getProfile(HttpServletRequest request);

    CompletableFuture<Long> countAllUser(HttpServletRequest request);

    CompletableFuture<Void> upgradeVendor(HttpServletRequest request);

    CompletableFuture<Integer> countMyAddress(HttpServletRequest request);

    CompletableFuture<Void> checkBalance(CheckBalanceRequest input, HttpServletRequest request);

    CompletableFuture<Void> cancelOrder(CancelOrderRequest input, HttpServletRequest request);

    CompletableFuture<Void> updateUserName(UpdateUserNameRequest input, HttpServletRequest request);

    CompletableFuture<InfoRatingResponse> getInfoForRating(String userId,
        HttpServletRequest request);

    CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(String keyword,
        Long skip,
        Integer size,
        String orderBy,
        EStatusBan isBan, HttpServletRequest request);

    CompletableFuture<Void> banUser(String userId, BanUserRequest input,
        HttpServletRequest request);

    CompletableFuture<UserResponse> getUserByAdmin(String userId, HttpServletRequest request);

}
