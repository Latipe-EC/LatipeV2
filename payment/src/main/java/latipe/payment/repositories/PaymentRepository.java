package latipe.payment.repositories;


import java.util.List;
import java.util.Optional;
import latipe.payment.entity.Payment;
import latipe.payment.entity.enumeration.EPaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Payment entity operations.
 * Provides methods for querying and manipulating payment records in the database.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Finds a payment by its associated order ID.
     *
     * @param orderId The order ID to search for
     * @return An Optional containing the matching Payment, if found
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Finds payments by a list of order IDs.
     *
     * @param orderId List of order IDs to search for
     * @return A list of matching Payment objects
     */
    @Query("{ 'orderId': { $in: ?0 } }")
    List<Payment> findByOrderIds(List<String> orderId);

    /**
     * Finds payments with pagination, filtered by a keyword and statuses.
     *
     * @param keyword Keyword to match against orderId or checkoutId
     * @param skip Number of records to skip
     * @param size Number of records to return
     * @param statuses List of payment statuses to include
     * @return A list of matching Payment objects
     */
    @Aggregation(pipeline = {
        "{ $match: { $and: [ { $or: [ { orderId: { $regex: ?0, $options: 'i' } }, { checkoutId: { $regex: ?0, $options: 'i' } } ] }, { paymentStatus: { $in: ?3 } } ] } }",
        "{ $skip: ?1 }", "{ $limit: ?2 }"
    })
    List<Payment> findPaginate(String keyword, Long skip, Integer size,
        List<EPaymentStatus> statuses);

    /**
     * Counts payments matching the provided search term and statuses.
     *
     * @param name Search term to match against orderId or checkoutId
     * @param statuses List of payment statuses to include
     * @return Count of matching payments
     */
    @Query(value = "{ $and: [ { $or: [ { 'orderId': { $regex: ?0, $options: 'i' } }, { 'checkoutId': { $regex: ?0, $options: 'i' } } ] }, { 'paymentStatus': { $in: ?1 } } ] }", count = true)
    Long countPayment(String name, List<EPaymentStatus> statuses);
}