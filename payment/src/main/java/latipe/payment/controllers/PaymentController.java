package latipe.payment.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.payment.annotations.ApiPrefixController;
import latipe.payment.annotations.Authenticate;
import latipe.payment.annotations.RequiresAuthorization;
import latipe.payment.annotations.SecureInternalPhase;
import latipe.payment.dtos.PagedResultDto;
import latipe.payment.entity.enumeration.EStatusFilter;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.PayByPaypalRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.request.TotalAmountRequest;
import latipe.payment.request.ValidWithdrawPaypalRequest;
import latipe.payment.request.WithdrawPaypalRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.response.PaymentResponse;
import latipe.payment.response.TotalAmountResponse;
import latipe.payment.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment-related operations.
 * Provides endpoints for creating, retrieving, and managing payments.
 * 
 * @author Latipe Development Team
 */
@RestController
@ApiPrefixController("payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Captures a payment based on the provided request.
     *
     * @param capturedPaymentRequest The request containing payment capture details
     * @param request The HTTP request
     * @return CompletableFuture containing the captured payment response
     */
    @PostMapping("/capture-payment")
    public CompletableFuture<CapturedPaymentResponse> capturePayment(
        @Valid @RequestBody CapturedPaymentRequest capturedPaymentRequest,
        HttpServletRequest request) {
        return paymentService.capturePayment(capturedPaymentRequest, request);
    }

    /**
     * Processes a payment order.
     *
     * @param input The payment order request
     * @param request The HTTP request
     * @return CompletableFuture representing the operation result
     */
    @PostMapping("/pay")
    public CompletableFuture<Void> validPayment(
        @Valid @RequestBody PayOrderRequest input, HttpServletRequest request) {
        return paymentService.payOrder(input, request);
    }

    /**
     * Retrieves payment order details by order ID.
     *
     * @param orderId The order ID
     * @param request The HTTP request
     * @return CompletableFuture containing the payment order response
     */
    @Authenticate
    @PostMapping("/payment-order/{orderId}")
    public CompletableFuture<CheckPaymentOrderResponse> getPayment(
        @PathVariable String orderId, HttpServletRequest request) {
        return paymentService.getPaymentOrder(orderId, request);
    }

    /**
     * Processes a payment using PayPal.
     *
     * @param input The PayPal payment request
     * @param request The HTTP request
     * @return CompletableFuture representing the operation result
     */
    @Authenticate
    @PostMapping("/capture-payments/paypal")
    public CompletableFuture<Void> payByPaypal(
        @Valid @RequestBody PayByPaypalRequest input, HttpServletRequest request) {

        return paymentService.payByPaypal(input, request);
    }

    /**
     * Checks the status of a PayPal order by order ID.
     *
     * @param orderId The order ID
     * @param request The HTTP request
     * @return CompletableFuture containing the payment order response
     */
    @Authenticate
    @GetMapping("/check-order-paypal/{orderId}")
    public CompletableFuture<CheckPaymentOrderResponse> checkOrderPaypal(
        @PathVariable String orderId, HttpServletRequest request) {

        return paymentService.checkOrderPaypal(orderId, request);
    }

    /**
     * Retrieves the total amount based on the provided request.
     *
     * @param input The total amount request
     * @param request The HTTP request
     * @return CompletableFuture containing the total amount response
     */
    @Authenticate
    @PostMapping("/total-amount")
    public CompletableFuture<TotalAmountResponse> getTotalAmount(
        @RequestBody TotalAmountRequest input, HttpServletRequest request) {

        return paymentService.getTotalAmount(input, request);
    }

    /**
     * Checks the status of an internal payment order by order ID.
     *
     * @param orderId The order ID
     * @param request The HTTP request
     * @return CompletableFuture containing the payment order response
     */
    @SecureInternalPhase
    @GetMapping("/check-order-internal/{orderId}")
    public CompletableFuture<CheckPaymentOrderResponse> checkPaymentInternal(
        @PathVariable String orderId, HttpServletRequest request) {
        return paymentService.checkPaymentInternal(orderId, request);
    }

    /**
     * Processes a PayPal withdrawal request.
     *
     * @param input The PayPal withdrawal request
     * @param request The HTTP request
     * @return CompletableFuture representing the operation result
     */
    @RequiresAuthorization("VENDOR")
    @PostMapping("/withdraw-paypal")
    public CompletableFuture<Void> withdrawPaypal(
        @Valid @RequestBody
        WithdrawPaypalRequest input, HttpServletRequest request) {

        return paymentService.withdrawPaypal(input, request);
    }

    /**
     * Validates a PayPal withdrawal request.
     *
     * @param input The valid PayPal withdrawal request
     * @param request The HTTP request
     * @return CompletableFuture representing the operation result
     */
    @RequiresAuthorization("VENDOR")
    @PostMapping("/valid-withdraw-paypal")
    public CompletableFuture<Void> validWithdrawPaypal(
        @Valid @RequestBody
        ValidWithdrawPaypalRequest input, HttpServletRequest request) {

        return paymentService.validWithdrawPaypal(input, request);
    }

    /**
     * Retrieves paginated payment results based on the provided filters.
     *
     * @param keyword The search keyword
     * @param skip The number of records to skip
     * @param size The number of records to retrieve
     * @param statusFilter The status filter
     * @param request The HTTP request
     * @return CompletableFuture containing the paginated payment results
     */
    @RequiresAuthorization("ADMIN")
    @GetMapping("/paginate")
    public CompletableFuture<PagedResultDto<PaymentResponse>> getPaginate(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Long skip,
        @RequestParam(defaultValue = "12") Integer size,
        @RequestParam(defaultValue = "ALL") EStatusFilter statusFilter,
        HttpServletRequest request) {
        return paymentService.getPaginate(keyword, skip, size, statusFilter, request);
    }
}
