package latipe.user.services.user;

import static latipe.user.constants.CONSTANTS.DEFAULT_PASSWORD;

import com.google.gson.Gson;
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
import latipe.user.response.UserResponse;
import latipe.user.utils.Constants;
import latipe.user.utils.GenerateUtils;
import latipe.user.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

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

  @Async
  @Override
  public CompletableFuture<UserResponse> getProfile(String id) {
    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(id)
          .orElseThrow(() -> new NotFoundException("User not found"));
      user.setRole(roleRepository.findById(user.getRoleId()).orElse(null));
      return userMapper.mapToResponse(user);
    });
  }

  @Async
  @Override
  public CompletableFuture<Long> countAllUser() {
    return CompletableFuture.supplyAsync(userRepository::count);
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
          new Pagination(user.getAddresses().size(), (long) (page - 1) * size, size),
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
      var address = userMapper.mapToUserAddress(new ObjectId().toString(), input);
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

      var username = GenerateUtils.generateRandomUsername();
      while (userRepository.existsByUsername(username)) {
        username = GenerateUtils.generateRandomUsername();
      }

      var user = userMapper.mapBeforeCreate(input,
          input.firstName() + " " + input.lastName(),
          passwordEncoder.encode(DEFAULT_PASSWORD), role.getId(), username);
      user.setIsBanned(false);
      var savedUser = userRepository.save(user);
      savedUser.setRole(role);

      var token = userMapper.mapToToken(savedUser.getId(), KeyType.VERIFY_ACCOUNT,
          ZonedDateTime.now().plusSeconds(expirationVerifyMs));
      token = tokenRepository.save(token);
      var tokenHash = TokenUtils.encodeToken(token.getId(), ENCRYPTION_KEY);

      // send mail verify account
      String message = gson.toJson(userMapper.mapToMessage(
          token.getUserId(), role.getName(), savedUser.getDisplayName(), savedUser.getEmail(),
          DEFAULT_PASSWORD, tokenHash));

      rabbitMQProducer.sendMessage(message, exchange,
          role.getName().equals(CONSTANTS.DELIVERY) ? routingDeliveryRegisterKey
              : routingUserRegisterKey);

      return UserResponse.fromUser(savedUser);
    });
  }

  @Override
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

      var username = GenerateUtils.generateRandomUsername();
      while (userRepository.existsByUsername(username)) {
        username = GenerateUtils.generateRandomUsername();
      }

      var user = userMapper.mapBeforeCreate(input, role.getId(),
          input.firstName() + " " + input.lastName(),
          passwordEncoder.encode(input.hashedPassword()), username);
      user.setIsBanned(false);
      var savedUser = userRepository.save(user);
      savedUser.setRole(role);

      var token = userMapper.mapToToken(savedUser.getId(), KeyType.VERIFY_ACCOUNT,
          ZonedDateTime.now().plusSeconds(expirationVerifyMs));
      token = tokenRepository.save(token);
      var tokenHash = TokenUtils.encodeToken(token.getId(), ENCRYPTION_KEY);

      // send mail verify account
      String message = gson.toJson(userMapper.mapToMessage(
          token.getUserId(), CONSTANTS.USER, savedUser.getDisplayName(), savedUser.getEmail(),
          null, tokenHash));
      rabbitMQProducer.sendMessage(message, exchange, routingUserRegisterKey);

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

  @Async
  @Override
  public CompletableFuture<Void> checkBalance(CheckBalanceRequest request) {

    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(request.userId())
          .orElseThrow(() -> new NotFoundException("User not found"));
      double money = Double.parseDouble(request.money().toString());
      if (user.getEWallet() < money) {
        throw new BadRequestException("Not enough money");
      }
      user.setEWallet(user.getEWallet() - money);
      userRepository.save(user);
      return null;
    });

  }

  @Async
  @Override
  public CompletableFuture<Void> cancelOrder(CancelOrderRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(request.userId())
          .orElseThrow(() -> new NotFoundException("User not found"));
      double money = Double.parseDouble(request.money().toString());
      user.setEWallet(user.getEWallet() + money);
      user.setPoint(user.getPoint() - Integer.parseInt(request.money().toString()) / 1000000);
      userRepository.save(user);
      return null;
    });
  }

  @Async
  @Override
  public CompletableFuture<Void> updateUserName(UpdateUserNameRequest request, String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(userId)
          .orElseThrow(() -> new NotFoundException("User not found"));

      if (user.getIsChangeUsername()) {
        throw new BadRequestException("You can only change username once");
      }

      if (userRepository.existsByUsername(request.username())) {
        throw new BadRequestException("Username already exists");
      }

      user.setUsername(request.username());
      user.setIsChangeUsername(true);
      userRepository.save(user);
      return null;
    });
  }

  @Async
  @Override
  public CompletableFuture<InfoRatingResponse> getInfoForRating(String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(userId)
          .orElse(null);

      if (user == null || user.getIsDeleted() || user.getVerifiedAt() == null) {
        return new InfoRatingResponse("user deleted", null);
      }
      return new InfoRatingResponse(user.getUsernameReal(), user.getAvatar());

    });
  }

  @Async
  @Override
  public CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(String keyword,
      Long skip,
      Integer size,
      String orderBy,
      EStatusBan ban) {
    return CompletableFuture.supplyAsync(() -> {

      Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
      String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;

      List<Boolean> banCriteria;
      if (ban == EStatusBan.ALL) {
        banCriteria = List.of(true, false);
      } else if (ban == EStatusBan.TRUE) {
        banCriteria = List.of(true);
      } else {
        banCriteria = List.of(false);
      }

      var criteriaSearch = new Criteria();
      criteriaSearch.orOperator(
          Criteria.where("email").regex(keyword, "i"),
          Criteria.where("phoneNumber").regex(keyword, "i"),
          Criteria.where("username").regex(keyword, "i")
      );

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

      return PagedResultDto.create(
          new Pagination(userRepository.countUserAdmin(banCriteria, keyword), skip, size),
          list);
    });
  }

  @Async
  @Override
  public CompletableFuture<Void> banUser(String userId, BanUserRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var user = userRepository.findById(userId)
          .orElseThrow(
              () -> new NotFoundException("User not found"));
      if (user.getIsBanned().equals(request.isBanned())) {
        throw new BadRequestException("User already banned");
      }
      user.setIsBanned(request.isBanned());
      if (request.isBanned()) {
        LOGGER.info("User {} is banned with reason {}", userId, request.reason());
        user.setReasonBan(request.reason());
      } else {
        LOGGER.info("User {} is unbanned", userId);
        user.setReasonBan(null);
      }
      userRepository.save(user);
      return null;

    });
  }

}
