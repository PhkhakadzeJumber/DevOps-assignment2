package ge.ticketebi.ticketebi_backend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(max = 32)
    private String ticketNumber;

    @Column(columnDefinition = "TEXT")
    private String seatInfo;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String qrCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_type"))
    private TicketTypeEntity ticketType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_order"))
    private OrderEntity order;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.VALID;

    @Column
    private LocalDateTime checkedInAt;

    public boolean isValid() {
        return status == TicketStatus.VALID;
    }

    public void checkIn() {
        if (status != TicketStatus.VALID) {
            throw new IllegalStateException("Ticket already used or invalid");
        }
        this.status = TicketStatus.USED;
        this.checkedInAt = LocalDateTime.now();
    }

    public String generateTicketNumber(Long eventId, Long typeId, Long number) {
        return String.format("T%d-%d-%d", eventId, typeId, number);
    }

}
