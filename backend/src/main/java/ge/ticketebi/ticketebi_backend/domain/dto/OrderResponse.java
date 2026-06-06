package ge.ticketebi.ticketebi_backend.domain.dto;

import ge.ticketebi.ticketebi_backend.domain.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String contactEmail;
    private BigDecimal subTotalAmount;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();

    @Builder.Default
    private List<TicketResponse> tickets = new ArrayList<>();
}
