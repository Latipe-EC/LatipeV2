package latipe.order.mappers;

import latipe.order.dtos.order.AdminOrderResponse;
import latipe.order.dtos.order.OrderDetailResponse;
import latipe.order.dtos.order.OrderItemResponse;
import latipe.order.dtos.order.OrderResponse;
import latipe.order.dtos.order.OrderShippingResponse;
import latipe.order.dtos.order.StoreOrderResponse;
import latipe.order.entity.Order;
import latipe.order.entity.OrderItem;
import latipe.order.entity.ProductVariant;
import latipe.order.viewmodel.OrderQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper interface for converting between Order entities and DTOs.
 * Uses MapStruct to automatically generate the implementation.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * Maps an Order entity to an OrderResponse DTO.
     *
     * @param order The Order entity to map
     * @return The mapped OrderResponse DTO
     */
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "createdDate", target = "orderDate")
    @Mapping(source = "totalPrice", target = "orderTotal")
    OrderResponse mapOrderToOrderResponse(Order order);

    /**
     * Maps an Order entity to an OrderDetailResponse DTO.
     *
     * @param order The Order entity to map
     * @return The mapped OrderDetailResponse DTO
     */
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "createdDate", target = "orderDate")
    @Mapping(source = "totalPrice", target = "orderTotal")
    OrderDetailResponse mapOrderToOrderDetailResponse(Order order);

    /**
     * Maps an OrderItem entity to an OrderItemResponse DTO.
     *
     * @param orderItem The OrderItem entity to map
     * @return The mapped OrderItemResponse DTO
     */
    @Mapping(target = "productName", source = "productVariant", qualifiedByName = "getProductName")
    @Mapping(target = "image", source = "productVariant.image")
    OrderItemResponse mapOrderItemToOrderItemResponse(OrderItem orderItem);

    /**
     * Maps an Order entity to a StoreOrderResponse DTO.
     *
     * @param order The Order entity to map
     * @return The mapped StoreOrderResponse DTO
     */
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "createdDate", target = "orderDate")
    @Mapping(source = "totalPrice", target = "orderTotal")
    StoreOrderResponse mapOrderToStoreOrderResponse(Order order);

    /**
     * Maps an Order entity to an AdminOrderResponse DTO.
     *
     * @param order The Order entity to map
     * @return The mapped AdminOrderResponse DTO
     */
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "createdDate", target = "orderDate")
    @Mapping(source = "totalPrice", target = "orderTotal")
    AdminOrderResponse mapOrderToAdminOrderResponse(Order order);

    /**
     * Named method to extract product name from a ProductVariant.
     * Used by MapStruct to resolve complex mappings.
     *
     * @param productVariant The ProductVariant containing the product name
     * @return The extracted product name
     */
    @Named("getProductName")
    default String getProductName(ProductVariant productVariant) {
        return productVariant.getName();
    }

    /**
     * Maps an Order entity to an OrderQuery view model.
     *
     * @param order The Order entity to map
     * @return The mapped OrderQuery view model
     */
    OrderQuery toOrderQuery(Order order);

    /**
     * Maps an Order entity to an OrderShippingResponse DTO.
     *
     * @param order The Order entity to map
     * @return The mapped OrderShippingResponse DTO
     */
    @Mapping(source = "shippingMethod", target = "method")
    @Mapping(source = "deliveryDate", target = "expectedDeliveryDate")
    OrderShippingResponse mapOrderToOrderShippingResponse(Order order);
}