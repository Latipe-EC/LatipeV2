package latipe.user.services.user;

import latipe.user.Entity.Role;
import latipe.user.Entity.User;
import latipe.user.Entity.UserAddress;
import latipe.user.exceptions.BadRequestException;
import latipe.user.exceptions.NotFoundException;
import latipe.user.repositories.IRoleRepository;
import latipe.user.repositories.IUserRepository;
import latipe.user.services.user.Dtos.CreateUserAddressDto;
import latipe.user.services.user.Dtos.UserCreateDto;
import latipe.user.services.user.Dtos.UserDto;
import latipe.user.services.user.Dtos.UserUpdateDto;
import latipe.user.utils.Constants;
import latipe.user.utils.NullAwareBeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final ModelMapper toDto;
    private final IRoleRepository roleRepository;

    public UserService(IUserRepository userRepository, ModelMapper toDto, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.toDto = toDto;
        this.roleRepository = roleRepository;
    }


    @Async
    public CompletableFuture<List<UserAddress>> getMyAddresses(String id, int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + page, user.getAddresses().size());
            if (startIndex >= endIndex) {
                return List.of();
            }
            return user.getAddresses().subList(startIndex, endIndex);
        });
    }

    @Async
    public CompletableFuture<UserAddress> addMyAddresses(String id, CreateUserAddressDto input) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
            UserAddress address = toDto.map(input, UserAddress.class);
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
    public CompletableFuture<UserAddress> updateMyAddresses(UserAddress input, String userId, String addressId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    BeanUtilsBean nullAwareBeanUtilsBean = NullAwareBeanUtilsBean.getInstance();
                    try {
                        nullAwareBeanUtilsBean.copyProperties(address, input);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    return address;
                }
            }
            throw new NotFoundException("Address not found");
        });
    }

    @Async
    @Override
    public CompletableFuture<UserDto> create(UserCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Role> role = roleRepository.findRoleByName(Constants.USER);
            if (userRepository.findByPhoneAndEmail(input.getEmail()).size() != 0)
                throw new BadRequestException("Email already exists");
            if (userRepository.findByPhoneAndEmail(input.getPhoneNumber()).size() != 0)
                throw new BadRequestException("Phone number already exists");
            User user = toDto.map(input, User.class);
            user.setRole(role.get());
            user.setDisplayName(input.getLastName() + " " + input.getFirstName());
            userRepository.save(user);
            toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            // send mail verrify account
            return toDto.map(user, UserDto.class);
        });
    }

    @Override
    public CompletableFuture<UserDto> update(String id, UserUpdateDto input) {
        return null;
    }

    @Override
    public CompletableFuture<Void> remove(String id) {
        return null;
    }

    @Override
    public CompletableFuture<List<UserDto>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<UserDto> getOne(String id) {
        return null;
    }
}
