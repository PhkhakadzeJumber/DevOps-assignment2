package ge.ticketebi.ticketebi_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantityTotal;

    private Integer quantityReserved;

    private Integer quantitySold;

    private Integer maxPurchase;

    private int availableQuantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleEndTime;

}