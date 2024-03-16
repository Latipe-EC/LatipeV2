package latipe.payment.services;


import static latipe.payment.utils.GenTokenInternal.generateHash;
import static latipe.payment.utils.GenTokenInternal.getPrivateKey;

import com.google.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.OrdersGetRequest;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.payment.configs.SecureInternalProperties;
import latipe.payment.dtos.PagedResultDto;
import latipe.payment.dtos.Pagination;
import latipe.payment.entity.Payment;
import latipe.payment.entity.Withdraw;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;
import latipe.payment.entity.enumeration.EStatusFilter;
import latipe.payment.entity.enumeration.EWithdrawStatus;
import latipe.payment.entity.enumeration.EWithdrawType;
import latipe.payment.exceptions.BadRequestException;
import latipe.payment.exceptions.ForbiddenException;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.feign.StoreClient;
import latipe.payment.feign.UserClient;
import latipe.payment.producer.RabbitMQProducer;
import latipe.payment.repositories.PaymentProviderRepository;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.repositories.WithdrawRepository;
import latipe.payment.request.CancelOrderRequest;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.CheckBalanceRequest;
import latipe.payment.request.CheckBalanceStoreRequest;
import latipe.payment.request.PayByPaypalRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.request.UpdateBalanceStoreRequest;
import latipe.payment.request.ValidWithdrawPaypalRequest;
import latipe.payment.request.WithdrawPaypalRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.response.PaymentResponse;
import latipe.payment.response.UserCredentialResponse;
import latipe.payment.utils.GetInstanceServer;
import latipe.payment.utils.TokenUtils;
import latipe.payment.viewmodel.OrderMessage;
import latipe.payment.viewmodel.OrderReplyMessage;
import latipe.payment.viewmodel.TokenWithdraw;
import latipe.payment.viewmodel.WithdrawMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      PaymentService.class);
  private final PaymentRepository paymentRepository;
  private final SecureInternalProperties secureInternalProperties;
  private final PayPalHttpClient payPalHttpClient;
  private final PaymentProviderRepository paymentProviderRepository;
  private final WithdrawRepository withdrawRepository;
  private final RabbitMQProducer rabbitMQProducer;
  private final Gson gson;
  private final LoadBalancerClient loadBalancer;
  private final GsonDecoder gsonDecoder;
  private final GsonEncoder gsonEncoder;
  private final OkHttpClient okHttpClient;


  @Value("${encryption.key}")
  private String ENCRYPTION_KEY;
  @Value("${expiration.withdraw-exps}")
  private Long withdrawExps;

  @Value("${rabbitmq.email.payment-withdraw-topic.routing.key}")
  private String topicWithdrawKey;
  @Value("${rabbitmq.email.exchange.name}")
  private String exchangeName;


  @Value("${rabbitmq.order.reply}")
  private String replyRoutingKey;
  @Value("${rabbitmq.order.exchange}")
  private String exchange;

  @Value("${service.auth}")
  private String authService;

  @Value("${service.user}")
  private String userService;

  @Value("${service.store}")
  private String storeService;

  @Async
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      CapturedPaymentRequest completedPayment) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = new Payment();
          payment.setOrderId(completedPayment.orderId());
          payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          payment.setPaymentFee(completedPayment.paymentFee());
          payment.setPaymentMethod(completedPayment.paymentMethod());
          payment.setAmount(completedPayment.amount());
          payment.setFailureMessage(completedPayment.failureMessage());
          payment.setGatewayTransactionId(completedPayment.gatewayTransactionId());
          payment.setCheckoutId(completedPayment.checkoutId());
          payment.setEmail(completedPayment.email());
          return CapturedPaymentResponse.fromModel(paymentRepository.save(payment));
        }
    );
  }

  @Async
  public CompletableFuture<Void> payOrder(
      PayOrderRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = paymentRepository.findByOrderId(request.orderId()).orElseThrow(
              () -> new NotFoundException("Cannot find order")
          );

          if (!payment.getPaymentStatus().equals(EPaymentStatus.PENDING)) {
            throw new BadRequestException("payment is not pending");
          }

          // check user
          String hash;
          try {
            hash = generateHash("user-service",
                getPrivateKey(secureInternalProperties.getPrivateKey()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          var userClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(UserClient.class,
                  String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )));
          userClient.checkBalance(hash,
              new CheckBalanceRequest(payment.getUserId(), payment.getAmount()));

          // if success update payment status
          payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          paymentRepository.save(payment);
          return null;
        }
    );
  }

  @Async
  public CompletableFuture<CheckPaymentOrderResponse> getPaymentOrder(
      String orderId) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = paymentRepository.findByOrderId(orderId).orElseThrow(
              () -> new NotFoundException("Cannot find order")
          );
          return CheckPaymentOrderResponse.fromModel(payment);
        }
    );
  }

  @Async
  public CompletableFuture<Void> payByPaypal(
      PayByPaypalRequest request, UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = paymentRepository.findByOrderId(request.orderId()).orElseThrow(
              () -> new NotFoundException("Cannot find order")
          );

          if (payment.getPaymentStatus().equals(EPaymentStatus.COMPLETED)) {
            throw new BadRequestException("Cannot pay for this");
          }

          if (!payment.getUserId().equals(userCredential.id())) {
            throw new ForbiddenException("you dont have permission to do this");
          }

          payment.setCheckoutId(request.id());
          if (request.status().equals("COMPLETED")) {
            payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          }
          payment.setEmail(request.email());
          paymentRepository.save(payment);
          return null;
        }
    );
  }

  @Async
  public CompletableFuture<Void> withdrawPaypal(
      WithdrawPaypalRequest request, UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          // check user
          String hash;
          try {
            hash = generateHash("store-service",
                getPrivateKey(secureInternalProperties.getPrivateKey()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          var storeClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(StoreClient.class,
                  String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, storeService
                  )));

          storeClient.checkBalance(hash,
              new CheckBalanceStoreRequest(userCredential.id(), request.amount()));

          var withdraw = withdrawRepository.findByUserIdAndWithdrawStatus(
                  userCredential.id(), EWithdrawStatus.PENDING)
              .orElse(null);
          if (withdraw != null) {
            withdraw.setWithdrawStatus(EWithdrawStatus.CANCELLED);
            withdrawRepository.save(withdraw);
          }

          var newWithdraw = Withdraw.builder()
              .emailRecipient(request.email())
              .amount(request.amount())
              .userId(userCredential.id())
              .withdrawStatus(EWithdrawStatus.PENDING)
              .type(EWithdrawType.PAYPAL)
              .build();
          newWithdraw = withdrawRepository.save(newWithdraw);

          // publish message to queue
          var tokenWithdraw = gson.toJson(new TokenWithdraw(
              newWithdraw.getId(),
              ZonedDateTime.now()
          ));

          var token = TokenUtils.encodeToken(tokenWithdraw, ENCRYPTION_KEY);
          var message = gson.toJson(new WithdrawMessage(
              userCredential.email(),
              newWithdraw.getAmount(),
              token,
              newWithdraw.getEmailRecipient(),
              newWithdraw.getType().name(),
              withdrawExps
          ));

          rabbitMQProducer.sendMessage(message, exchangeName, topicWithdrawKey);
          LOGGER.info("Create new withdraw amount with withdraw id: {}", newWithdraw.getId());
          LOGGER.info("Token withdraw: %s, with id: %s".formatted(token, newWithdraw.getId()));
          return null;
        }
    );
  }

  @Async
  public CompletableFuture<Void> validWithdrawPaypal(
      ValidWithdrawPaypalRequest request, UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          var tokenWithdraw = gson.fromJson(TokenUtils.decodeToken(request.token(), ENCRYPTION_KEY),
              TokenWithdraw.class);

          if (tokenWithdraw == null || ZonedDateTime.now()
              .isAfter(tokenWithdraw.createdAt().plusSeconds(withdrawExps))) {
            throw new BadRequestException("Token is invalid");
          }

          var withdraw = withdrawRepository.findById(tokenWithdraw.id()).orElseThrow(
              () -> new NotFoundException("Cannot find withdraw")
          );

          if (!withdraw.getUserId().equals(userCredential.id())) {
            throw new ForbiddenException("you dont have permission to do this");
          }

          if (!withdraw.getWithdrawStatus().equals(EWithdrawStatus.PENDING)) {
            throw new BadRequestException("Cannot withdraw amount");
          }

          // call api withdraw amount
          String hash;
          try {
            hash = generateHash("store-service",
                getPrivateKey(secureInternalProperties.getPrivateKey()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          try {
            var storeClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(StoreClient.class,
                    String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, storeService
                    )));

            storeClient.updateBalance(hash,
                new UpdateBalanceStoreRequest(userCredential.id(), withdraw.getAmount()));
          } catch (Exception e) {
            LOGGER.error("Cannot withdraw amount with withdraw id: {}, update status to cancel",
                withdraw.getId());
            withdraw.setWithdrawStatus(EWithdrawStatus.CANCELLED);
            withdrawRepository.save(withdraw);
            throw new BadRequestException("Cannot withdraw amount");
          }

          okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
          Request req;
          try {
            req = requestTransferMoney(withdraw.getEmailRecipient(),
                withdraw.getAmount().divide(new BigDecimal(23000), 2, RoundingMode.CEILING));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          try {
            Response response = client.newCall(req).execute();
            if (!response.isSuccessful()) {
              throw new BadRequestException("Cannot withdraw amount");
            }
          } catch (IOException e) {
            LOGGER.error("Cannot withdraw amount with withdraw id: {}, proceed with refund",
                withdraw.getId());

            var storeClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(StoreClient.class,
                    String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, storeService
                    )));

            storeClient.updateBalance(hash,
                new UpdateBalanceStoreRequest(userCredential.id(), withdraw.getAmount().negate()));
            throw new BadRequestException("Cannot withdraw amount");
          }
          withdraw.setWithdrawStatus(EWithdrawStatus.COMPLETED);
          withdrawRepository.save(withdraw);
          LOGGER.info("Complete withdraw amount with withdraw id: {}", withdraw.getId());
          return null;
        }
    );
  }

  @Async
  public CompletableFuture<CheckPaymentOrderResponse> checkOrderPaypal(String orderId,
      UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = paymentRepository.findByOrderId(orderId).orElseThrow(
              () -> new NotFoundException("Cannot find order")
          );

          if (!payment.getUserId().equals(userCredential.id())) {
            throw new ForbiddenException("you dont have permission to do this");
          }

          OrdersGetRequest request = new OrdersGetRequest(payment.getCheckoutId());
          try {
            var response = payPalHttpClient.execute(request);
            if (response.statusCode() != 200) {
              throw new BadRequestException("Cannot get order");
            }
            var order = response.result();
            if (order.status().equals("COMPLETED")) {
              payment.setPaymentStatus(EPaymentStatus.COMPLETED);
            } else if (order.status().equals("APPROVED")) {
              payment.setPaymentStatus(EPaymentStatus.PENDING);
            } else {
              payment.setPaymentStatus(EPaymentStatus.CANCELLED);
            }
            paymentRepository.save(payment);

          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          paymentRepository.save(payment);
          return CheckPaymentOrderResponse.fromModel(payment);
        }
    );
  }

  @Async
  public CompletableFuture<CheckPaymentOrderResponse> checkPaymentInternal(String orderId) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = paymentRepository.findByOrderId(orderId).orElseThrow(
              () -> new NotFoundException("Cannot find order")
          );

          if (payment.getPaymentMethod().equals(EPaymentMethod.PAYPAL)
              && payment.getCheckoutId() != null) {
            OrdersGetRequest request = new OrdersGetRequest(payment.getCheckoutId());
            try {
              var response = payPalHttpClient.execute(request);
              if (response.statusCode() != 200) {
                throw new BadRequestException("Cannot get order");
              }

              var order = response.result();
              if (order.status().equals("COMPLETED")) {
                payment.setPaymentStatus(EPaymentStatus.COMPLETED);
              } else if (order.status().equals("APPROVED")) {
                payment.setPaymentStatus(EPaymentStatus.PENDING);
              } else {
                payment.setPaymentStatus(EPaymentStatus.CANCELLED);
              }


            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          // check time is over 2 days
          var cal = Calendar.getInstance();
          cal.setTime(payment.getCreatedDate());
          cal.add(Calendar.DATE, 2);
          cal.set(Calendar.HOUR_OF_DAY, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
          var twoDaysFromNow = cal.getTime();

          if (payment.getCreatedDate().after(twoDaysFromNow)) {
            payment.setPaymentStatus(EPaymentStatus.CANCELLED);
          }
          payment = paymentRepository.save(payment);
          return CheckPaymentOrderResponse.fromModel(payment);
        }
    );
  }

  @Async
  public CompletableFuture<PagedResultDto<PaymentResponse>> getPaginate(
      String keyword,
      Long skip,
      Integer size,
      EStatusFilter statusFilter) {
    return CompletableFuture.supplyAsync(
        () -> {
          List<EPaymentStatus> statuses;
          if (statusFilter.equals(EStatusFilter.ALL)) {
            statuses = List.of(EPaymentStatus.values());
          } else if (statusFilter.equals(EStatusFilter.PENDING)) {
            statuses = List.of(EPaymentStatus.PENDING);
          } else if (statusFilter.equals(EStatusFilter.COMPLETED)) {
            statuses = List.of(EPaymentStatus.COMPLETED);
          } else {
            statuses = List.of(EPaymentStatus.CANCELLED);
          }
          var payments = paymentRepository.findPaginate(keyword, skip, size, statuses);
          var total = paymentRepository.countPayment(keyword, statuses);
          return new PagedResultDto<>(
              new Pagination(total, skip, size),
              payments.stream().map(PaymentResponse::fromModel).toList()
          );
        }
    );
  }

  private String getAccessToken() throws IOException {

    var paymentProvider = paymentProviderRepository.findById("PaypalPayment")
        .orElseThrow(()
            -> new NotFoundException("PAYMENT_PROVIDER_NOT_FOUND", "PaypalPayment"));

    JsonObject settingsJson = JsonParser.parseString(paymentProvider.getAdditionalSettings())
        .getAsJsonObject();
    String clientId = settingsJson.get("clientId").getAsString();
    String clientSecret = settingsJson.get("clientSecret").getAsString();
    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

    String credentials = Credentials.basic(clientId, clientSecret);

    RequestBody body = new FormBody.Builder()
        .add("grant_type", "client_credentials")
        .build();

    Request request = new Request.Builder()
        .url("https://api.sandbox.paypal.com/v1/oauth2/token")
        .post(body)
        .header("Accept", "application/json")
        .header("Accept-Language", "en_US")
        .header("Authorization", credentials)
        .build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        throw new BadRequestException("Cannot get access token");
      }
      // Get response body
      String responseBody = response.body().string();
      JsonObject responseJson = JsonParser.parseString(responseBody)
          .getAsJsonObject();
      response.close();
      return responseJson.get("access_token").getAsString();
    }
  }

  public void handleOrderCreate(
      OrderMessage message) {
    var payment = new Payment();
    payment.setOrderId(message.orderId());
    payment.setAmount(message.amount());
    payment.setPaymentStatus(EPaymentStatus.PENDING);
    payment.setUserId(message.userId());
    payment.setPaymentMethod(
        message.paymentMethod() == 1 ? EPaymentMethod.COD : EPaymentMethod.PAYPAL);

    paymentRepository.save(payment);

    rabbitMQProducer.sendMessage(gson.toJson(
        OrderReplyMessage.create(1, message.orderId())), exchange, replyRoutingKey);
  }

  public void handleUserCancelOrder(
      OrderMessage message) {
    var payment = paymentRepository.findByOrderId(message.orderId()).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );

    if (!payment.getPaymentMethod().equals(EPaymentMethod.COD)
        && payment.getPaymentStatus().equals(EPaymentStatus.COMPLETED)
    ) {
      // call api refund amount and minus point
      String hash;
      try {
        hash = generateHash("user-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var userClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
          .decoder(gsonDecoder).target(UserClient.class,
              String.format("%s/api/v1", GetInstanceServer.get(
                  loadBalancer, userService
              )));

      userClient.cancelOrder(hash,
          new CancelOrderRequest(payment.getUserId(), payment.getAmount()));
      payment.setPaymentStatus(EPaymentStatus.CANCELLED);
      paymentRepository.save(payment);
    }
  }

  public Payment handleFinishShipping(
      OrderMessage message) {
    var payment = paymentRepository.findByOrderId(message.orderId()).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );
    payment.setPaymentStatus(EPaymentStatus.COMPLETED);
    return paymentRepository.save(payment);
  }

  public void handleRollbackOrder(String orderId) {
    var payment = paymentRepository.findByOrderId(orderId).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );

    var withdraw = withdrawRepository.findByOrderId(orderId).orElse(null);

    // IMPORTANT: if payment is already completed, do not throw exception because it causes rollback
    if (payment.getIsRefund() && withdraw != null) {
      LOGGER.info("Payment with order id: {} is already refund", orderId);
      return;
    }

    Request req;
    try {
      req = requestTransferMoney(payment.getEmail(),
          payment.getAmount().divide(new BigDecimal(23000), 2, RoundingMode.CEILING));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // save payment before refund
    payment.setIsRefund(true);
    paymentRepository.save(payment);

    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    try {
      var response = client.newCall(req).execute();
      if (!response.isSuccessful()) {
        payment.setIsRefund(false);
        paymentRepository.save(payment);
        throw new BadRequestException("FAILED:Paypal");
      }
    } catch (IOException e) {
      payment.setIsRefund(false);
      paymentRepository.save(payment);
      throw new BadRequestException("FAILED:Paypal");
    }

    try {
      var newWithdraw = Withdraw.builder()
          .emailRecipient(payment.getEmail())
          .amount(payment.getAmount())
          .userId(payment.getUserId())
          .withdrawStatus(EWithdrawStatus.COMPLETED)
          .type(EWithdrawType.PAYPAL)
          .orderId(payment.getOrderId())
          .build();
      withdrawRepository.save(newWithdraw);
    } catch (Exception e) {
      LOGGER.error("Cannot create withdraw amount with payment id: {}, proceed with refund",
          payment.getId());
      // remember check
      throw new BadRequestException("FAILED:Withdraw");
    }

    LOGGER.info("Complete refund amount with payment id: {}", payment.getId());
  }

  private Request requestTransferMoney(
      String emailRecipient, BigDecimal amount) throws IOException {
    MediaType mediaType = MediaType.parse("application/json");
    var body = RequestBody.create(
        "{\n  \"sender_batch_header\": {\n    \"email_subject\": \"You have a payment\"\n  },\n  \"items\": [\n    {\n      \"recipient_type\": \"EMAIL\",\n      \"amount\": {\n        \"value\": "
            + amount
            + ",\n        \"currency\": \"" + "USD"
            + "\"\n      },\n      \"receiver\": \"" + emailRecipient
            + "\",\n      \"note\": \"Complete withdraw!\",\n      \"sender_item_id\": \"item1\"\n    }\n  ]\n}",
        mediaType);

    return new Request.Builder()
        .url("https://api.sandbox.paypal.com/v1/payments/payouts")
        .post(body)
        .addHeader("content-type", "application/json")
        .addHeader("authorization", "Bearer %s".formatted(getAccessToken()))
        .build();
  }
}
