package ge.ticketebi.ticketebi_backend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="ticket_types")
public class TicketTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(max = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Min(value = 1)
    private Integer quantityTotal;

    @Column(nullable = false)
    @Min(value = 0)
    private Integer quantityReserved = 0;

    @Column(nullable = false)
    @Min(value = 0)
    private Integer quantitySold = 0;

    @Column(nullable = false)
    @Min(value = 1)
    private Integer maxPurchase = 10;

    @Column(nullable = false)
    private LocalDateTime saleStartTime;

    @Column(nullable = false)
    private LocalDateTime saleEndTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_event"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventEntity event;

    @AssertTrue(message = "sold and reserved tickets sum cannot exceed total quantity")
    public boolean isQuantityValid() {
        return quantitySold + quantityReserved <= quantityTotal;
    }

    public int getAvailableQuantity() {
        return quantityTotal - (quantityReserved + quantitySold);
    }

    public boolean canPurchaseQuantity(int quantity) {
        return quantity <= maxPurchase && quantity <= getAvailableQuantity();
    }

    public boolean reserveTickets(int quantity) {
        if(canPurchaseQuantity(quantity)) {
            quantityReserved += quantity;
            return true;
        }

        return false;
    }

    public void releaseReservedTickets(int quantity) {
        quantityReserved = Math.max(0, quantityReserved - quantity);
    }

    public boolean confirmPurchase(int quantity) {
        if(quantityReserved >= quantity) {
            quantityReserved -= quantity;
            quantitySold += quantity;
            return true;
        }

        return false;
    }

}
