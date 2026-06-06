package ge.ticketebi.ticketebi_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ge.ticketebi.ticketebi_backend.validation.ValidSalePeriod;
import jakarta.validation.constraints.*;
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
@ValidSalePeriod
public class TicketTypeRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Min(value = 1)
    private Integer quantityTotal;

    @NotNull
    @Min(value = 1)
    private Integer maxPurchase;

    @NotNull
    @FutureOrPresent(message = "Event date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime saleStartTime;

    @NotNull
    @FutureOrPresent(message = "Event date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime saleEndTime;
}
