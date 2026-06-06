package ge.ticketebi.ticketebi_backend.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderItemRequest {
    @NotNull(message = "ticketTypeId is required")
    private Long ticketTypeId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
}
