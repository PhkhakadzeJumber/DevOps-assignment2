package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.*;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.services.EventService;
import ge.ticketebi.ticketebi_backend.services.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @RequestBody @Valid EventRequest eventRequest,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponse event = eventService.createEvent(eventRequest, organizer);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents() {
        List<EventResponse> events = eventService.getEvents();
        return ResponseEntity.ok(events);
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<EventResponse>> myEvents(@AuthenticationPrincipal User organizer) {
        List<EventResponse> events = eventService.myEvents(organizer);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User organizer
    ) {
        MessageResponse response = eventService.deleteEvent(id, organizer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody @Valid EventUpdateRequest updateRequest,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponse updated = eventService.updateEvent(id, updateRequest, organizer);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/thumbnail")
    public ResponseEntity<MessageResponse> uploadEventThumbnail(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal User organizer
    ) {
        MessageResponse imageUrl = eventService.uploadThumbnail(id, file, organizer);
        return ResponseEntity.ok(imageUrl);
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/ticket-type")
    public TicketTypeResponse addTicketType(
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeRequest ticketTypeRequest,
            @AuthenticationPrincipal User organizer
    ) {
        return ticketTypeService.addTicketType(ticketTypeRequest, id, organizer);
    }

    @GetMapping("/{id}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypesByEvent(id));
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PatchMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponse> updateTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId,
            @Valid @RequestBody TicketTypeUpdateRequest request,
            @AuthenticationPrincipal User actor
    ) {
        return ResponseEntity.ok(ticketTypeService.updateTicketType(eventId, ticketTypeId, request, actor));
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<MessageResponse> deleteTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId,
            @AuthenticationPrincipal User actor
    ) {
        return ResponseEntity.ok(ticketTypeService.deleteTicketType(eventId, ticketTypeId, actor));
    }
}
