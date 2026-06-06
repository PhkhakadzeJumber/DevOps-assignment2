package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.EventRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.EventResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.EventUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventCreateRequest, User organizer);
    List<EventResponse> getEvents();
    List<EventResponse> myEvents(User organizer);
    EventResponse getEventById(Long id);
    MessageResponse deleteEvent(Long id, User organizer);
    EventResponse updateEvent(Long id, EventUpdateRequest request, User organizer);
    MessageResponse uploadThumbnail(Long id, MultipartFile file, User organizer);
}
