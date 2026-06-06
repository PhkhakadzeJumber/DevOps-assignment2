package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.TicketRepository;
import ge.ticketebi.ticketebi_backend.security.qr.QrTokenService;
import ge.ticketebi.ticketebi_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final Mapper<TicketEntity, TicketResponse> ticketMapper;
    private final QrTokenService qrTokenService;

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(User user) {
        return ticketRepository.findAllByUser(user)
                .stream()
                .map(ticketMapper::mapTo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId, User user) {
        TicketEntity ticket = ticketRepository.findByIdAndUser(ticketId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return ticketMapper.mapTo(ticket);
    }

    @Override
    public MessageResponse verifyAndCheckInByQr(String qrCode, User actor) {
        String ticketNumber = qrTokenService.extractTicketNumber(qrCode);
        TicketEntity ticket = ticketRepository.findByTicketNumberWithDetails(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!qrCode.equals(ticket.getQrCode())) {
            throw new InvalidRequestException("QR token does not match the active ticket token");
        }

        return checkInTicket(ticket, actor);
    }

    private MessageResponse checkInTicket(TicketEntity ticket, User actor) {
        if (!canCheckIn(ticket, actor)) {
            throw new UnauthorizedActionException("You are not allowed to check in this ticket");
        }
        try {
            ticket.checkIn();
        } catch (IllegalStateException ex) {
            throw new InvalidRequestException(ex.getMessage());
        }
        ticketRepository.save(ticket);
        return new MessageResponse("Ticket checked in successfully");
    }

    private boolean canCheckIn(TicketEntity ticket, User actor) {
        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isOrganizer = actor.getRole() == Role.ORGANIZER;
        boolean ownsEvent = ticket.getTicketType().getEvent().getOrganizer().getId().equals(actor.getId());
        return isAdmin || (isOrganizer && ownsEvent);
    }
}
