package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.*;
import ge.ticketebi.ticketebi_backend.domain.entities.*;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.mappers.ReqResMapper;
import ge.ticketebi.ticketebi_backend.repositories.CategoryRepository;
import ge.ticketebi.ticketebi_backend.repositories.EventRepository;
import ge.ticketebi.ticketebi_backend.repositories.LocationRepository;
import ge.ticketebi.ticketebi_backend.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final ReqResMapper<EventEntity, EventRequest, EventResponse> eventMapper;
    private final Mapper<LocationEntity, LocationDto> locationMapper;
    private final Mapper<CategoryEntity, CategoryDto> categoryMapper;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;

    public EventResponse createEvent(EventRequest eventRequest, User organizer) {
        EventEntity event = eventMapper.toEntity(eventRequest);
        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        Set<CategoryEntity> categories = eventRequest.getCategories().stream()
                .map(catDto -> categoryRepository.findByName(catDto.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + catDto.getName())))
                .collect(Collectors.toSet());

        event.setCategories(categories);

        Optional<LocationEntity> existingLoc = locationRepository.findByName(event.getLocation().getName());

        LocationEntity location = existingLoc.orElseGet(() -> locationRepository.save(event.getLocation()));

        event.setLocation(location);

        eventRepository.save(event);

        return eventMapper.toResponseDto(event);
    }

    @Override
    public List<EventResponse> getEvents(){
        return eventRepository.findAllWithDetails()
                .stream()
                .map(eventMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> myEvents(User organizer) {
        return eventRepository.findByOrganizerWithDetails(organizer)
                .stream()
                .map(eventMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(Long id){
        EventEntity event = eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return eventMapper.toResponseDto(event);
    }

    @Override
    public MessageResponse deleteEvent(Long id, User organizer) {
        EventEntity event = eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("event not found"));

        if(!organizer.getRole().equals(Role.ADMIN) && !event.getOrganizer().getId().equals(organizer.getId()))
            throw new UnauthorizedActionException("You are not allowed to delete this event");

        // soft delete
        event.setDeleted(true);
        eventRepository.save(event);

        return new MessageResponse("Event deleted");
    }

    @Override
    public EventResponse updateEvent(Long id, EventUpdateRequest request, User organizer){
        EventEntity event = eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("event not found"));

        if(!organizer.getRole().equals(Role.ADMIN) && !event.getOrganizer().getId().equals(organizer.getId()))
            throw new UnauthorizedActionException("You are not allowed to perform update");

        if(request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if(request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if(request.getDate() != null) {
            event.setDate(request.getDate());
        }
        if(request.getLocation() != null) {
            //  update existing or create new
            LocationEntity loc = locationRepository.findByName(request.getLocation().getName())
                    .orElseGet(() -> locationMapper.mapFrom(request.getLocation()));
            loc.setAddress(request.getLocation().getAddress());
            loc.setCity(request.getLocation().getCity());
            loc.setLatitude(request.getLocation().getLatitude());
            loc.setLongitude(request.getLocation().getLongitude());
            locationRepository.save(loc);
            event.setLocation(loc);
        }

        if(request.getCategories() != null && !request.getCategories().isEmpty()) {
            Set<CategoryEntity> categories = request.getCategories().stream()
                    .map(dto -> categoryRepository.findByName(dto.getName())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Category not found: " + dto.getName())))
                    .collect(Collectors.toSet());
            event.setCategories(categories);
        }

        eventRepository.save(event);

        return eventMapper.toResponseDto(event);
    }

    public MessageResponse uploadThumbnail(Long eventId, MultipartFile file, User organizer) {
        EventEntity event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if(!organizer.getRole().equals(Role.ADMIN) && !event.getOrganizer().getId().equals(organizer.getId()))
            throw new UnauthorizedActionException("You are not allowed to update this event");

        if (file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }

        try {
            String uploadDir = "/app/uploads/events/" + eventId;
            Files.createDirectories(Paths.get(uploadDir));

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/events/" + eventId + "/" + filename;
            event.setThumbnailUrl(imageUrl);
            eventRepository.save(event);

            return new MessageResponse(imageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }
}
