package latipe.user.services.user;

import java.util.concurrent.CompletableFuture;

import latipe.user.dtos.PagedResultDto;
import latipe.user.dtos.Pagination;
import latipe.user.entity.User;
import latipe.user.entity.UserAddress;
import latipe.user.exceptions.BadRequestException;
import latipe.user.exceptions.NotFoundException;
import latipe.user.mappers.IUserMapper;
import latipe.user.repositories.IRoleRepository;
import latipe.user.repositories.IUserRepository;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.UserResponse;
import latipe.user.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserMapper userMapper;

    @Async
    @Override
    public CompletableFuture<UserResponse> getProfile(String id) {
        return CompletableFuture.supplyAsync(() -> {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            return userMapper.mapToResponse(user);
        });
    }

    @Async
    @Override
    public CompletableFuture<UserResponse> updateProfile(String id, UpdateUserRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userMapper.mapBeforeUpdateUser(user, input);
            user = userRepository.save(user);
            return userMapper.mapToResponse(user);
        });
    }

    @Async
    @Override
    public CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(String id, int page,
                                                                         int size) {
        return CompletableFuture.supplyAsync(() -> {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, user.getAddresses().size());
            if (startIndex >= endIndex) {
                return null;
            }
            return PagedResultDto.create(
                    new Pagination(user.getAddresses().size(), (page - 1) * size, size),
                    user.getAddresses().subList(startIndex, endIndex));
        });
    }

    @Async
    @Override
    public CompletableFuture<UserAddress> addMyAddresses(String id, CreateUserAddressRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            if (user.getAddresses().size() > 9) {
                throw new BadRequestException("You can only add up to 10 addresses");
            }
            var address = UserAddress.builder().id(new ObjectId().toString())
                    .contactName(input.contactName()).phone(input.phone())
                    .detailAddress(input.detailAddress()).zipCode(input.zipCode())
                    .countryId(input.countryId()).countryName(input.countryName())
                    .cityOrProvinceId(input.cityOrProvinceId()).cityOrProvinceName(input.cityOrProvinceName())
                    .districtId(input.districtId()).districtName(input.districtName()).wardId(input.wardId())
                    .wardName(input.districtName()).build();
            user.getAddresses().add(address);
            userRepository.save(user);
            return address;
        });
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteMyAddresses(String id, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(id)) {
                    user.getAddresses().remove(address);
                    userRepository.save(user);
                    return null;
                }
            }
            throw new NotFoundException("Address not found with id: %s".formatted(id));
        });
    }

    @Async
    @Override
    public CompletableFuture<UserAddress> getMyAddresses(String id, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(id)) {
                    return address;
                }
            }
            throw new NotFoundException("Address not found with id: %s".formatted(id));
        });
    }

    @Async
    @Override
    public CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
                                                            String userId, String addressId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    userMapper.mapBeforeUpdateUserAddress(address, input);
                    userRepository.save(user);
                    return address;
                }
            }
            throw new NotFoundException("Address not found");
        });
    }

    @Async
    @Override
    public CompletableFuture<UserResponse> create(CreateUserRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            var role = roleRepository.findRoleByName(input.role()).orElseThrow(
                    () -> new NotFoundException("Have error from server, please try again later"));
            if (!userRepository.findByPhoneAndEmail(input.email()).isEmpty()) {
                throw new BadRequestException("Email already exists");
            }
            if (!userRepository.findByPhoneAndEmail(input.phoneNumber()).isEmpty()) {
                throw new BadRequestException("Phone number already exists");
            }

            var user = userMapper.mapBeforeCreateUserAddress(input,
                    input.firstName() + " " + input.lastName(),
                    passwordEncoder.encode(input.hashedPassword()));
            user.setRoleId(role.getId());
            var savedUser = userRepository.save(user);
            // send mail verify account
            return UserResponse.fromUser(savedUser);
        });
    }

    @Override
    @Transactional
    public CompletableFuture<UserResponse> register(RegisterRequest input) {

        return CompletableFuture.supplyAsync(() -> {
            var role = roleRepository.findRoleByName(Constants.USER).orElseThrow(
                    () -> new NotFoundException("Have error from server, please try again later"));
            if (!userRepository.findByPhoneAndEmail(input.email()).isEmpty()) {
                throw new BadRequestException("Email already exists");
            }
            if (!userRepository.findByPhoneAndEmail(input.phoneNumber()).isEmpty()) {
                throw new BadRequestException("Phone number already exists");
            }

            var user = userMapper.mapBeforeCreate(input, role.getId(),
                    input.firstName() + " " + input.lastName(),
                    passwordEncoder.encode(input.hashedPassword()));
            var savedUser = userRepository.save(user);
            savedUser.setRole(role);
            // send mail verify account
            return UserResponse.fromUser(savedUser);
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> upgradeVendor(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var vendorRole = roleRepository.findRoleByName(Constants.VENDOR)
                .orElseThrow(() -> new NotFoundException("Have error from server, please try again later"));
        user.setRoleId(vendorRole.getId());
        userRepository.save(user);
        return null;
    }

    @Async
    @Override
    public CompletableFuture<Integer> countMyAddress(String userId) {

        return CompletableFuture.supplyAsync(() -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            return user.getAddresses().size();
        });

    }
}
