package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.CreateOrderRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.OrderResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.User;

import java.util.List;

public interface OrderService {
    OrderResponse createDraftOrder(CreateOrderRequest request, User user);

    OrderResponse confirmOrder(Long orderId, User user);

    OrderResponse cancelOrder(Long orderId, User user);

    List<OrderResponse> getMyOrders(User user);

    OrderResponse getMyOrderById(Long orderId, User user);
}
