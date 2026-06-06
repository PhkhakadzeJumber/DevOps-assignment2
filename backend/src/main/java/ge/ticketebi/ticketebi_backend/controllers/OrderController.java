package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.CreateOrderRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.OrderResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createDraftOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        OrderResponse response = orderService.createDraftOrder(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(orderService.confirmOrder(id, user));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(id, user));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getMyOrders(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(orderService.getMyOrderById(id, user));
    }
}
