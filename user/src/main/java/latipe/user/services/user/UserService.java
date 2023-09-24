package latipe.user.services.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.user.Entity.Role;
import latipe.user.Entity.User;
import latipe.user.Entity.UserAddress;
import latipe.user.exceptions.BadRequestException;
import latipe.user.exceptions.NotFoundException;
import latipe.user.repositories.IRoleRepository;
import latipe.user.repositories.IUserRepository;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.response.UserResponse;
import latipe.user.utils.Constants;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepository, IRoleRepository roleRepository,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Async
    public CompletableFuture<List<UserAddress>> getMyAddresses(String id, int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            var user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + page, user.getAddresses().size());
            if (startIndex >= endIndex) {
                return List.of();
            }
            return user.getAddresses().subList(startIndex, endIndex);
        });
    }

    @Async
    public CompletableFuture<UserAddress> addMyAddresses(String id,
        CreateUserAddressRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
            if (user.getAddresses().size() > 9)
                throw new BadRequestException("You can only add up to 10 addresses");
            var address = UserAddress.builder()
                .id(new ObjectId().toString())
                .contactName(input.contactName())
                .phone(input.phone())
                .detailAddress(input.detailAddress())
                .zipCode(input.zipCode())
                .city(input.city())
                .countryId(input.countryId())
                .countryName(input.countryName())
                .stateOrProvinceId(input.stateOrProvinceId())
                .stateOrProvinceName(input.stateOrProvinceName())
                .districtId(input.districtId())
                .districtName(input.districtName())
                .build();
            user.getAddresses().add(address);
            userRepository.save(user);
            return address;
        });
    }

    @Async
    public CompletableFuture<Void> deleteMyAddresses(String id) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(id)) {
                    user.getAddresses().remove(address);
                    break;
                }
            }
            return null;
        });
    }

    @Async
    public CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
        String userId, String addressId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    address = UserAddress.builder()
                        .id(addressId)
                        .contactName(input.contactName())
                        .phone(input.phone())
                        .detailAddress(input.detailAddress())
                        .zipCode(input.zipCode())
                        .city(input.city())
                        .countryId(input.countryId())
                        .countryName(input.countryName())
                        .stateOrProvinceId(input.stateOrProvinceId())
                        .stateOrProvinceName(input.stateOrProvinceName())
                        .districtId(input.districtId())
                        .districtName(input.districtName())
                        .build();
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
                () -> new NotFoundException("Have error from server, please try again later")
            );
            if (!userRepository.findByPhoneAndEmail(input.email()).isEmpty())
                throw new BadRequestException("Email already exists");
            if (!userRepository.findByPhoneAndEmail(input.phoneNumber()).isEmpty())
                throw new BadRequestException("Phone number already exists");

            var user = User.builder()
                .firstName(input.firstName())
                .lastName(input.lastName())
                .phoneNumber(input.phoneNumber())
                .email(input.email())
                .hashedPassword(passwordEncoder.encode(input.hashedPassword()))
                .avatar(input.avatar())
                .displayName(input.firstName() + " " + input.lastName())
                .role(role)
                .build();
            userRepository.save(user);
            // send mail verrify account
            return UserResponse.fromUser(user);
        });
    }

    @Async
    @Override
    public CompletableFuture<UserResponse> register(RegisterRequest input) {

        return CompletableFuture.supplyAsync(() -> {
            var role = roleRepository.findRoleByName(Constants.USER).orElseThrow(
                () -> new NotFoundException("Have error from server, please try again later")
            );
            if (!userRepository.findByPhoneAndEmail(input.email()).isEmpty()) {
                throw new BadRequestException("Email already exists");
            }
            if (!userRepository.findByPhoneAndEmail(input.phoneNumber()).isEmpty()) {
                throw new BadRequestException("Phone number already exists");
            }

            var user = User.builder()
                .firstName(input.firstName())
                .lastName(input.lastName())
                .phoneNumber(input.phoneNumber())
                .email(input.email())
                .hashedPassword(passwordEncoder.encode(input.hashedPassword()))
                .avatar(input.avatar())
                .displayName(input.firstName() + " " + input.lastName())
                .role(role)
                .build();
            userRepository.save(user);
            // send mail verrify account
            return UserResponse.fromUser(user);
        });
    }


}
