package ge.ticketebi.ticketebi_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeUpdateRequest {

    @Size(min = 1, max = 255, message = "Name must not be blank")
    private String name;

    @Size(min = 1, message = "Description must not be blank")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Min(value = 1)
    private Integer quantityTotal;

    @Min(value = 1)
    private Integer maxPurchase;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime saleStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime saleEndTime;
}
