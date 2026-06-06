package ge.ticketebi.ticketebi_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketScanRequest {
    @NotBlank(message = "qrCode is required")
    private String qrCode;
}
