package ge.ticketebi.ticketebi_backend.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotEmpty(message = "items are required")
    @Valid
    @Builder.Default
    private List<CreateOrderItemRequest> items = new ArrayList<>();

    @Email(message = "contactEmail must be a valid email")
    private String contactEmail;
}
