package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.entities.User;

import java.util.List;

public interface TicketTypeService {
    TicketTypeResponse addTicketType(TicketTypeRequest ticketType, Long eventId, User organizer);

    List<TicketTypeResponse> getTicketTypesByEvent(Long eventId);

    TicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, TicketTypeUpdateRequest request, User actor);

    MessageResponse deleteTicketType(Long eventId, Long ticketTypeId, User actor);
}
