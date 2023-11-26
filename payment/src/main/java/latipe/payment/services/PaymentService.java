package latipe.payment.services;


import static latipe.payment.constants.CONSTANTS.URL;
import static latipe.payment.utils.GenTokenInternal.generateHash;
import static latipe.payment.utils.GenTokenInternal.getPrivateKey;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.util.concurrent.CompletableFuture;
import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import latipe.payment.configs.SecureInternalProperties;
import latipe.payment.exceptions.BadRequestException;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.feign.UserClient;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.request.CancelOrderRequest;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.CheckBalanceRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.viewmodel.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final SecureInternalProperties secureInternalProperties;

  @Async
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      CapturedPaymentRequest completedPayment) {
    return CompletableFuture.supplyAsync(
        () -> {
          Payment payment = Payment.builder()
              .checkoutId(completedPayment.checkoutId())
              .orderId(completedPayment.orderId())
              .paymentStatus(completedPayment.paymentStatus())
              .paymentFee(completedPayment.paymentFee())
              .paymentMethod(completedPayment.paymentMethod())
              .amount(completedPayment.amount())
              .failureMessage(completedPayment.failureMessage())
              .gatewayTransactionId(completedPayment.gatewayTransactionId())
              .build();
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

          var userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
              .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
              .target(UserClient.class, URL);

          String hash;
          try {
            hash = generateHash("user-service",
                getPrivateKey(secureInternalProperties.getPrivateKey()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          userClient.checkBalance(hash,
              new CheckBalanceRequest(payment.getUserId(), payment.getAmount()));

          // if success update payment status
          payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          paymentRepository.save(payment);
          return null;
        }
    );
  }

  public void handleOrderCreate(
      OrderMessage message) {
    var payment = new Payment();
    payment.setOrderId(message.orderUuid());
    payment.setAmount(message.amount());
    payment.setPaymentStatus(EPaymentStatus.PENDING);
    payment.setUserId(message.userRequest().userId());
    payment.setPaymentMethod(
        message.paymentMethod() == 1 ? EPaymentMethod.COD : EPaymentMethod.PAYPAL);

    paymentRepository.save(payment);
  }

  public void handleUserCancelOrder(
      OrderMessage message) {
    var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );

    if (!payment.getPaymentMethod().equals(EPaymentMethod.COD)
        && payment.getPaymentStatus().equals(EPaymentStatus.COMPLETED)
    ) {
      // call api refund money and minus point
      var userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
          .decoder(new GsonDecoder()).logLevel(feign.Logger.Level.FULL)
          .target(UserClient.class, URL);

      String hash;
      try {
        hash = generateHash("user-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      userClient.cancelOrder(hash,
          new CancelOrderRequest(payment.getUserId(), payment.getAmount()));
      payment.setPaymentStatus(EPaymentStatus.CANCELLED);
      paymentRepository.save(payment);
    }
  }

  public void handleFinishShipping(
      OrderMessage message) {
    var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );
    payment.setPaymentStatus(EPaymentStatus.COMPLETED);
    paymentRepository.save(payment);
  }
}
