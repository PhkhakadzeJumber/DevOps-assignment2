package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.entities.EventEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketTypeEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.mappers.ReqResMapper;
import ge.ticketebi.ticketebi_backend.repositories.EventRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketTypeRepository;
import ge.ticketebi.ticketebi_backend.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketTypeServiceImpl implements TicketTypeService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final ReqResMapper<TicketTypeEntity, TicketTypeRequest, TicketTypeResponse> ticketTypeMapper;

    @Override
    public TicketTypeResponse addTicketType(TicketTypeRequest ticketType, Long eventId, User organizer) {
        EventEntity event = eventRepository.findByIdAndDeletedFalse(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event doesn't exist")
        );
        ensureCanManageEvent(event, organizer);

        TicketTypeEntity ticketTypeEntity = ticketTypeMapper.toEntity(ticketType);
        ticketTypeEntity.setEvent(event);

        ticketTypeRepository.save(ticketTypeEntity);

        return ticketTypeMapper.toResponseDto(ticketTypeEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event doesn't exist"));

        return ticketTypeRepository.findByEventId(eventId).stream()
                .map(ticketTypeMapper::toResponseDto)
                .toList();
    }

    @Override
    public TicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, TicketTypeUpdateRequest request, User actor) {
        EventEntity event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event doesn't exist"));
        ensureCanManageEvent(event, actor);

        TicketTypeEntity ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
        ensureSaleNotStarted(ticketType);

        if (request.getName() != null) {
            ticketType.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            ticketType.setDescription(request.getDescription().trim());
        }
        if (request.getPrice() != null) {
            ticketType.setPrice(request.getPrice());
        }
        if (request.getQuantityTotal() != null) {
            int minAllowed = ticketType.getQuantityReserved() + ticketType.getQuantitySold();
            if (request.getQuantityTotal() < minAllowed) {
                throw new InvalidRequestException("quantityTotal cannot be less than reserved + sold");
            }
            ticketType.setQuantityTotal(request.getQuantityTotal());
        }
        if (request.getMaxPurchase() != null) {
            ticketType.setMaxPurchase(request.getMaxPurchase());
        }
        if (request.getSaleStartTime() != null) {
            ticketType.setSaleStartTime(request.getSaleStartTime());
        }
        if (request.getSaleEndTime() != null) {
            ticketType.setSaleEndTime(request.getSaleEndTime());
        }

        if (!ticketType.getSaleStartTime().isBefore(ticketType.getSaleEndTime())) {
            throw new InvalidRequestException("saleStartTime must be before saleEndTime");
        }
        if (ticketType.getMaxPurchase() > ticketType.getQuantityTotal()) {
            throw new InvalidRequestException("maxPurchase cannot be greater than quantityTotal");
        }

        ticketTypeRepository.save(ticketType);
        return ticketTypeMapper.toResponseDto(ticketType);
    }

    @Override
    public MessageResponse deleteTicketType(Long eventId, Long ticketTypeId, User actor) {
        EventEntity event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event doesn't exist"));
        ensureCanManageEvent(event, actor);

        TicketTypeEntity ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
        ensureSaleNotStarted(ticketType);

        ticketTypeRepository.delete(ticketType);
        return new MessageResponse("Ticket type deleted");
    }

    private void ensureCanManageEvent(EventEntity event, User actor) {
        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isOwnerOrganizer = actor.getRole() == Role.ORGANIZER
                && event.getOrganizer().getId().equals(actor.getId());
        if (!isAdmin && !isOwnerOrganizer) {
            throw new UnauthorizedActionException("You don't have permission to perform this action");
        }
    }

    private void ensureSaleNotStarted(TicketTypeEntity ticketType) {
        if (!ticketType.getSaleStartTime().isAfter(LocalDateTime.now())) {
            throw new InvalidRequestException("Ticket type can be changed only before sale starts");
        }
    }
}
