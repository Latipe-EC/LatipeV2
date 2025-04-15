package latipe.user.services.user;

import static latipe.user.constants.CONSTANTS.DEFAULT_PASSWORD;
import static latipe.user.utils.AuthenticationUtils.getMethodName;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.user.constants.CONSTANTS;
import latipe.user.constants.EStatusBan;
import latipe.user.constants.KeyType;
import latipe.user.dtos.PagedResultDto;
import latipe.user.dtos.Pagination;
import latipe.user.entity.User;
import latipe.user.entity.UserAddress;
import latipe.user.exceptions.BadRequestException;
import latipe.user.exceptions.NotFoundException;
import latipe.user.mappers.UserMapper;
import latipe.user.producer.RabbitMQProducer;
import latipe.user.repositories.IRoleRepository;
import latipe.user.repositories.ITokenRepository;
import latipe.user.repositories.IUserRepository;
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
import latipe.user.response.UserCredentialResponse;
import latipe.user.response.UserResponse;
import latipe.user.utils.Constants;
import latipe.user.utils.GenerateUtils;
import latipe.user.utils.TokenUtils;
import latipe.user.viewmodel.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user operations.
 * Handles user CRUD operations, profile management, address management,
 * wallet operations, and administrative functions.
 * 
 * @author Latipe Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ITokenRepository tokenRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final MongoTemplate mongoTemplate;
    private final Gson gson;

    @Value("${rabbitmq.email.exchange.name}")
    private String exchange;
    
    @Value("${rabbitmq.email.user-register-topic.routing.key}")
    private String routingUserRegisterKey;
    
    @Value("${rabbitmq.email.delivery-register-topic.routing.key}")
    private String routingDeliveryRegisterKey;
    
    @Value("${encryption.key}")
    private String ENCRYPTION_KEY;
    
    @Value("${expiration.verification}")
    private Long expirationVerifyMs;

    /**
     * Gets the current authenticated user's profile.
     *
     * @param request The HTTP request containing user authentication
     * @return CompletableFuture containing the user response
     */
    @Async
    @Override
    public CompletableFuture<UserResponse> getProfile(HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Get profile [id: %s]".formatted(userId), request,
                getMethodName())));
        
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            user.setRole(roleRepository.findById(user.getRoleId()).orElse(null));
            log.info("Get profile successfully [id: %s]".formatted(userId));
            return userMapper.mapToResponse(user);
        });
    }

    /**
     * Gets user details for an administrator.
     *
     * @param userId ID of the user to retrieve
     * @param request The HTTP request
     * @return CompletableFuture containing the user response
     */
    @Async
    @Override
    public CompletableFuture<UserResponse> getUserByAdmin(String userId,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get user by admin [id: %s]".formatted(userId), request,
                getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            user.setRole(roleRepository.findById(user.getRoleId()).orElse(null));
            log.info("Get user by admin successfully [id: %s]".formatted(userId));
            return userMapper.mapToResponse(user);
        });
    }

    /**
     * Counts total number of users in the system.
     *
     * @param request The HTTP request
     * @return CompletableFuture containing the count of users
     */
    @Async
    @Override
    public CompletableFuture<Long> countAllUser(HttpServletRequest request) {
        log.info(gson.toJson(LogMessage.create("Count all user", request, getMethodName())));
        return CompletableFuture.supplyAsync(userRepository::count);
    }

    /**
     * Updates a user's profile information.
     *
     * @param input Update request containing new profile information
     * @param request The HTTP request
     * @return CompletableFuture containing the updated user response
     */
    @Async
    @Override
    public CompletableFuture<UserResponse> updateProfile(UpdateUserRequest input,
        HttpServletRequest request) {
        String userId = getUserId(request);

        log.info(gson.toJson(
            LogMessage.create("Update profile [id: %s]".formatted(userId), request, getMethodName())));
            
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            userMapper.mapBeforeUpdateUser(user, input);
            user = userRepository.save(user);
            log.info("Update profile successfully [id: %s]".formatted(userId));
            return userMapper.mapToResponse(user);
        });
    }

    /**
     * Gets a paginated list of user addresses.
     *
     * @param page Page number
     * @param size Page size
     * @param request The HTTP request
     * @return CompletableFuture containing paginated user addresses
     */
    @Async
    @Override
    public CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(int page,
        int size, HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Get my addresses [id: %s]".formatted(userId), request,
                getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, user.getAddresses().size());
            
            if (startIndex >= endIndex) {
                // Return empty result instead of null
                return PagedResultDto.create(
                    new Pagination(user.getAddresses().size(), (long) (page - 1) * size, size),
                    List.of());
            }

            log.info("Get my addresses successfully [id: %s]".formatted(userId));
            return PagedResultDto.create(
                new Pagination(user.getAddresses().size(), (long) (page - 1) * size, size),
                user.getAddresses().subList(startIndex, endIndex));
        });
    }

    /**
     * Adds a new address to the user's addresses.
     *
     * @param input Create address request
     * @param request The HTTP request
     * @return CompletableFuture containing the created address
     */
    @Async
    @Override
    public CompletableFuture<UserAddress> addMyAddresses(CreateUserAddressRequest input,
        HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Add my address [id: %s]".formatted(userId), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);

            String addressId = new ObjectId().toString();
            if (user.getAddresses().size() > 9) {
                throw new BadRequestException("You can only add up to 10 addresses");
            }
            
            UserAddress address = userMapper.mapToUserAddress(addressId, input);
            user.getAddresses().add(address);
            userRepository.save(user);

            log.info(
                "Add my address successfully [user-id: %s], [address-id: %s]".formatted(userId,
                    addressId));
            return address;
        });
    }

    /**
     * Deletes an address from the user's addresses.
     *
     * @param addressId ID of the address to delete
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Async
    @Override
    public CompletableFuture<Void> deleteMyAddresses(String addressId, HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create(
                "Delete my address [user-id: %s], [address-id: %s]".formatted(userId, addressId),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
                
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    user.getAddresses().remove(address);
                    userRepository.save(user);
                    log.info(
                        "Delete my address successfully [user-id: %s], [address-id: %s]".formatted(
                            userId, addressId));
                    return null;
                }
            }
            
            throw new NotFoundException("Address not found with id: %s".formatted(addressId));
        });
    }

    /**
     * Gets a specific address by ID.
     *
     * @param addressId ID of the address to retrieve
     * @param request The HTTP request
     * @return CompletableFuture containing the address
     */
    @Async
    @Override
    public CompletableFuture<UserAddress> getMyAddresses(String addressId,
        HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create(
                "Get my address [user-id: %s], [address-id: %s]".formatted(userId, addressId),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    log.info(
                        "Get my address successfully [user-id: %s], [address-id: %s]".formatted(
                            userId, addressId));
                    return address;
                }
            }
            
            throw new NotFoundException("Address not found with id: %s".formatted(addressId));
        });
    }

    /**
     * Updates a specific address.
     *
     * @param input Update address request
     * @param addressId ID of the address to update
     * @param request The HTTP request
     * @return CompletableFuture containing the updated address
     */
    @Async
    @Override
    public CompletableFuture<UserAddress> updateMyAddresses(UpdateUserAddressRequest input,
        String addressId, HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create(
                "Update my address [user-id: %s], [address-id: %s]".formatted(userId, addressId),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            
            for (UserAddress address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    userMapper.mapBeforeUpdateUserAddress(address, input);
                    userRepository.save(user);
                    log.info(
                        "Update my address successfully [user-id: %s], [address-id: %s]".formatted(
                            userId, addressId));
                    return address;
                }
            }
            
            throw new NotFoundException("Address not found");
        });
    }

    /**
     * Creates a new user.
     *
     * @param input Create user request
     * @param request The HTTP request
     * @return CompletableFuture containing the created user
     */
    @Async
    @Override
    public CompletableFuture<UserResponse> create(CreateUserRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Create user [email: %s]".formatted(input.email()), request,
                getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            validateNewUserEmailAndPhone(input.email(), input.phoneNumber());
            
            var role = roleRepository.findRoleByName(input.role()).orElseThrow(
                () -> new NotFoundException("Have error from server, please try again later"));
            
            String username = generateUniqueUsername();
            
            User user = userMapper.mapBeforeCreate(input,
                input.firstName() + " " + input.lastName(),
                passwordEncoder.encode(DEFAULT_PASSWORD), role.getId(), username);
            user.setIsBanned(false);
            User savedUser = userRepository.save(user);
            savedUser.setRole(role);

            sendVerificationEmail(savedUser, role.getName());

            log.info("Create user successfully [id: %s], [email: %s]".formatted(savedUser.getId(),
                input.email()));
            return UserResponse.fromUser(savedUser);
        });
    }

    /**
     * Registers a new user.
     *
     * @param input Register request
     * @param request The HTTP request
     * @return CompletableFuture containing the registered user
     */
    @Override
    public CompletableFuture<UserResponse> register(RegisterRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Register [email: %s]".formatted(input.email()), request,
                getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            validateNewUserEmailAndPhone(input.email(), input.phoneNumber());
            
            var role = roleRepository.findRoleByName(Constants.USER).orElseThrow(
                () -> new NotFoundException("Have error from server, please try again later"));

            String username = generateUniqueUsername();

            User user = userMapper.mapBeforeCreate(input, role.getId(),
                input.firstName() + " " + input.lastName(),
                passwordEncoder.encode(input.hashedPassword()), username);
            user.setIsBanned(false);
            User savedUser = userRepository.save(user);
            savedUser.setRole(role);

            sendVerificationEmail(savedUser, CONSTANTS.USER);

            log.info("Register successfully [id: %s], [email: %s]".formatted(savedUser.getId(),
                input.email()));
            return UserResponse.fromUser(savedUser);
        });
    }

    /**
     * Upgrades a user to vendor status.
     *
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Override
    @Async
    public CompletableFuture<Void> upgradeVendor(HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Upgrade to vendor [id: %s]".formatted(userId), request,
                getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var user = findUserById(userId);

            var vendorRole = roleRepository.findRoleByName(Constants.VENDOR)
                .orElseThrow(
                    () -> new NotFoundException("Have error from server, please try again later"));
            user.setRoleId(vendorRole.getId());
            userRepository.save(user);
            log.info("Upgrade to vendor successfully [id: %s]".formatted(userId));
            return null;
        });
    }

    /**
     * Counts the number of addresses for the current user.
     *
     * @param request The HTTP request
     * @return CompletableFuture containing the address count
     */
    @Async
    @Override
    public CompletableFuture<Integer> countMyAddress(HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Count my address [id: %s]".formatted(userId), request,
                getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            log.info("Count my address successfully [id: %s]".formatted(userId));
            return user.getAddresses().size();
        });
    }

    /**
     * Checks if a user has enough balance for a purchase.
     * Deducts the amount if sufficient funds are available.
     *
     * @param input Check balance request
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Async
    @Override
    public CompletableFuture<Void> checkBalance(CheckBalanceRequest input,
        HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create(
                "Check balance [id: %s], [money: %s]".formatted(userId, input.money()),
                request, getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(input.userId());
            double money = Double.parseDouble(input.money().toString());
            
            if (user.getEWallet() < money) {
                throw new BadRequestException("Not enough money");
            }
            
            user.setEWallet(user.getEWallet() - money);
            userRepository.save(user);
            log.info("Check balance successfully [id: %s], [balance: %s]".formatted(userId,
                user.getEWallet()));
            return null;
        });
    }

    /**
     * Handles order cancellation by refunding the amount to the user's wallet.
     *
     * @param input Cancel order request
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Async
    @Override
    public CompletableFuture<Void> cancelOrder(CancelOrderRequest input,
        HttpServletRequest request) {
        var userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Cancel order [id: %s], [money: %s]".formatted(userId, input.money()),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var user = findUserById(input.userId());
            var money = new BigDecimal(input.money().toString());
            var pointsToDeduct = money.intValue() / 1000000;
            
            // Prevent negative point balance
            if (user.getPoint() < pointsToDeduct) {
                log.warn("Cannot deduct {} points from user {} with only {} points", 
                    pointsToDeduct, userId, user.getPoint());
                pointsToDeduct = user.getPoint(); // Deduct only available points
            }
            
            user.setEWallet(user.getEWallet() + money.doubleValue());
            user.setPoint(user.getPoint() - pointsToDeduct);
            user = userRepository.save(user);
            
            log.info("Cancel order successfully [id: %s], [balance: %s], [points: %s]".formatted(
                userId, user.getEWallet(), user.getPoint()));
            return null;
        });
    }

    /**
     * Updates a user's username.
     *
     * @param input Update username request
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Async
    @Override
    public CompletableFuture<Void> updateUserName(UpdateUserNameRequest input,
        HttpServletRequest request) {
        String userId = getUserId(request);
        log.info(gson.toJson(
            LogMessage.create("Update username [id: %s], [username: %s]".formatted(userId,
                    input.username()),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);

            String oldUsername = user.getUsername();
            if (user.getIsChangeUsername()) {
                throw new BadRequestException("You can only change username once");
            }

            if (userRepository.existsByUsername(input.username())) {
                throw new BadRequestException("Username already exists");
            }

            user.setUsername(input.username());
            user.setIsChangeUsername(true);
            userRepository.save(user);
            log.info(
                "Update username successfully [id: %s], [old username: %s], [new username: %s]".formatted(
                    userId, oldUsername,
                    input.username()));
            return null;
        });
    }

    /**
     * Gets user information for rating purposes.
     *
     * @param userId ID of the user to get rating info for
     * @param request The HTTP request
     * @return CompletableFuture containing rating information
     */
    @Async
    @Override
    public CompletableFuture<InfoRatingResponse> getInfoForRating(String userId,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get info for rating [id: %s]".formatted(userId), request,
                getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                .orElse(null);

            log.info("Get info for rating successfully [id: %s]".formatted(userId));
            if (user == null || user.getIsDeleted() || user.getVerifiedAt() == null) {
                return new InfoRatingResponse("user deleted", null);
            }
            return new InfoRatingResponse(user.getUsernameReal(), user.getAvatar());
        });
    }

    /**
     * Gets a paginated list of users for admin purposes.
     *
     * @param keyword Search keyword
     * @param skip Number of items to skip
     * @param size Page size
     * @param orderBy Order by field
     * @param ban Ban status filter
     * @param request The HTTP request
     * @return CompletableFuture containing paginated user responses
     */
    @Async
    @Override
    public CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(String keyword,
        Long skip, Integer size, String orderBy, EStatusBan ban, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get user admin [keyword: %s], [skip: %s], [size: %s], [orderBy: %s], [ban: %s]".formatted(
                    keyword, skip, size, orderBy, ban), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            List<Boolean> banCriteria = getBanCriteria(ban);

            var criteriaSearch = new Criteria();
            criteriaSearch.orOperator(
                Criteria.where("email").regex(keyword, "i"),
                Criteria.where("phoneNumber").regex(keyword, "i"),
                Criteria.where("username").regex(keyword, "i")
            );

            Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
            String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;

            var aggregate = Aggregation.newAggregation(User.class,
                Aggregation.match(
                    Criteria.where("isBanned").in(banCriteria)
                        .andOperator(criteriaSearch)
                ),
                Aggregation.lookup("Roles", "roleId", "_id", "role"),
                Aggregation.skip(skip), Aggregation.limit(size),
                Aggregation.sort(direction, orderByField));

            var results = mongoTemplate.aggregate(aggregate, User.class, Document.class);
            var documents = results.getMappedResults();

            var list = documents.stream().map(doc -> UserAdminResponse.builder()
                    .avatar(doc.getString("avatar"))
                    .displayName(doc.getString("displayName"))
                    .email(doc.getString("email"))
                    .eWallet(Double.parseDouble(doc.get("eWallet").toString()))
                    .id(doc.get("_id").toString())
                    .isBanned(doc.getBoolean("isBanned"))
                    .isDeleted(doc.getBoolean("isDeleted"))
                    .phoneNumber(doc.getString("phoneNumber"))
                    .point(doc.getInteger("point"))
                    .username(doc.getString("username"))
                    .role(doc.getList("role", Document.class).get(0).getString("name"))
                    .reasonBan(doc.getString("reasonBan"))
                    .gender(doc.getString("gender"))
                    .birthday(doc.getDate("birthday"))
                    .build())
                .toList();

            log.info(
                "Get user admin successfully [keyword: %s], [skip: %s], [size: %s], [orderBy: %s], [ban: %s]".formatted(
                    keyword, skip, size, orderBy, ban));

            return PagedResultDto.create(
                new Pagination(userRepository.countUserAdmin(banCriteria, keyword), skip, size),
                list);
        });
    }

    /**
     * Bans or unbans a user.
     *
     * @param userId ID of the user to ban/unban
     * @param input Ban user request
     * @param request The HTTP request
     * @return CompletableFuture representing the completion of the operation
     */
    @Async
    @Override
    public CompletableFuture<Void> banUser(String userId, BanUserRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Ban user [id: %s], [isBanned: %s]".formatted(userId, input.isBanned()),
                request, getMethodName())));
                
        return CompletableFuture.supplyAsync(() -> {
            User user = findUserById(userId);
            
            if (user.getIsBanned().equals(input.isBanned())) {
                throw new BadRequestException("User already banned");
            }
            
            user.setIsBanned(input.isBanned());
            if (input.isBanned()) {
                log.info("User {} is banned with reason {}", userId, input.reason());
                user.setReasonBan(input.reason());
            } else {
                log.info("User {} is unbanned", userId);
                user.setReasonBan(null);
            }
            userRepository.save(user);
            log.info(
                "Ban user successfully [id: %s], [isBanned: %s]".formatted(userId,
                    input.isBanned()));
            return null;
        });
    }

    /**
     * Gets the user ID from the request.
     *
     * @param request The HTTP request
     * @return The user ID
     */
    private String getUserId(HttpServletRequest request) {
        return ((UserCredentialResponse) request.getAttribute("user")).id();
    }
    
    /**
     * Finds a user by ID or throws a NotFoundException.
     *
     * @param userId The user ID
     * @return The user entity
     * @throws NotFoundException If user not found
     */
    private User findUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
    
    /**
     * Validates that email and phone number are not already in use.
     *
     * @param email Email to validate
     * @param phoneNumber Phone number to validate
     * @throws BadRequestException If email or phone already exists
     */
    private void validateNewUserEmailAndPhone(String email, String phoneNumber) {
        if (!userRepository.findByPhoneAndEmail(email).isEmpty()) {
            throw new BadRequestException("Email already exists");
        }
        if (!userRepository.findByPhoneAndEmail(phoneNumber).isEmpty()) {
            throw new BadRequestException("Phone number already exists");
        }
    }
    
    /**
     * Generates a unique username that doesn't exist in the database.
     *
     * @return A unique username
     */
    private String generateUniqueUsername() {
        String username = GenerateUtils.generateRandomUsername();
        while (userRepository.existsByUsername(username)) {
            username = GenerateUtils.generateRandomUsername();
        }
        return username;
    }
    
    /**
     * Sends a verification email to a newly registered user.
     *
     * @param user The user to send verification to
     * @param roleName The role name of the user
     */
    private void sendVerificationEmail(User user, String roleName) {
        var token = userMapper.mapToToken(user.getId(), KeyType.VERIFY_ACCOUNT,
            ZonedDateTime.now().plusSeconds(expirationVerifyMs));
        token = tokenRepository.save(token);
        var tokenHash = TokenUtils.encodeToken(token.getId(), ENCRYPTION_KEY);

        // send mail verify account
        String message = gson.toJson(userMapper.mapToMessage(
            token.getUserId(), roleName, user.getDisplayName(), user.getEmail(),
            null, tokenHash));
            
        String routingKey = roleName.equals(CONSTANTS.DELIVERY) ? 
            routingDeliveryRegisterKey : routingUserRegisterKey;
            
        rabbitMQProducer.sendMessage(message, exchange, routingKey);
    }
    
    /**
     * Gets the criteria for ban status filtering.
     *
     * @param ban Ban status enum
     * @return List of boolean values corresponding to the ban status
     */
    private List<Boolean> getBanCriteria(EStatusBan ban) {
        if (ban == EStatusBan.ALL) {
            return List.of(true, false);
        } else if (ban == EStatusBan.TRUE) {
            return List.of(true);
        } else {
            return List.of(false);
        }
    }
}
