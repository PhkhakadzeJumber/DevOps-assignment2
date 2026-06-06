package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketScanRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.getMyTickets(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getMyTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ticketService.getMyTicketById(id, user));
    }

    @PostMapping("/verify-and-check-in")
    public ResponseEntity<MessageResponse> verifyAndCheckInTicket(
            @Valid @RequestBody TicketScanRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ticketService.verifyAndCheckInByQr(request.getQrCode(), user));
    }
}
