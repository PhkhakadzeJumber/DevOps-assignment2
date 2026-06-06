package ge.ticketebi.ticketebi_backend.domain.dto;

import ge.ticketebi.ticketebi_backend.domain.entities.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private String qrCode;
    private String seatInfo;
    private String email;
    private TicketStatus status;
    private LocalDateTime checkedInAt;
    private Long ticketTypeId;
    private String ticketTypeName;
}
