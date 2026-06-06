package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.User;

import java.util.List;

public interface TicketService {
    List<TicketResponse> getMyTickets(User user);

    TicketResponse getMyTicketById(Long ticketId, User user);

    MessageResponse verifyAndCheckInByQr(String qrCode, User actor);
}
