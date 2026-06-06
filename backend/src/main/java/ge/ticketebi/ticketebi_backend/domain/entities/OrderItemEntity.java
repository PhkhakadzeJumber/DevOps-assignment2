package ge.ticketebi.ticketebi_backend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_order"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_ticket_type"))
    private TicketTypeEntity ticketType;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal lineTotal;
}
