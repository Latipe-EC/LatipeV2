package latipe.payment.services;


import static latipe.payment.constants.CONSTANTS.URL;
import static latipe.payment.utils.GenTokenInternal.generateHash;
import static latipe.payment.utils.GenTokenInternal.getPrivateKey;

import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.OrdersGetRequest;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import latipe.payment.configs.SecureInternalProperties;
import latipe.payment.entity.Payment;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;
import latipe.payment.exceptions.BadRequestException;
import latipe.payment.exceptions.ForbiddenException;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.feign.UserClient;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.request.CancelOrderRequest;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.CheckBalanceRequest;
import latipe.payment.request.PayByPaypalRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.response.UserCredentialResponse;
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
  private final PayPalHttpClient payPalHttpClient;

  @Async
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      CapturedPaymentRequest completedPayment) {
    return CompletableFuture.supplyAsync(
        () -> {
          var payment = Payment.builder()
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
          paymentRepository.save(payment);
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
    }
  }

  public Payment handleFinishShipping(
      OrderMessage message) {
    var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
        () -> new NotFoundException("Not found payment")
    );
    payment.setPaymentStatus(EPaymentStatus.COMPLETED);
    return paymentRepository.save(payment);
  }
}
