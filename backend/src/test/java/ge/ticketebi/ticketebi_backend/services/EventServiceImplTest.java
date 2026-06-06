package ge.ticketebi.ticketebi_backend.services;

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
import ge.ticketebi.ticketebi_backend.services.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private ReqResMapper<EventEntity, EventRequest, EventResponse> eventMapper;
    @Mock private Mapper<LocationEntity, LocationDto> locationMapper;

    @InjectMocks private EventServiceImpl eventService;

    private CategoryEntity categoryEntity;
    private CategoryDto categoryDto;

    private LocationEntity locationEntity;
    private LocationDto locationDto;

    private EventEntity eventEntity;
    private EventRequest eventRequest;
    private EventUpdateRequest eventUpdateRequest;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        // -------- CATEGORY --------
        categoryEntity = CategoryEntity.builder()
                .id(1L)
                .name("Music")
                .build();

        categoryDto = CategoryDto.builder()
                .name("Music")
                .build();

        // -------- LOCATION --------
        locationEntity = LocationEntity.builder()
                .id(1L)
                .name("Tbilisi Concert Hall")
                .address("Rustaveli Ave 1")
                .city("Tbilisi")
                .latitude(41.7151)
                .longitude(44.8271)
                .build();

        locationDto = LocationDto.builder()
                .name("Tbilisi Concert Hall")
                .address("Rustaveli Ave 1")
                .city("Tbilisi")
                .latitude(41.7151)
                .longitude(44.8271)
                .build();

        // -------- EVENT REQUEST (used for creating event) --------
        eventRequest = EventRequest.builder()
                .title("Rock Festival")
                .description("Annual rock music festival")
                .date(LocalDateTime.of(2025, 10, 20, 18, 0))
                .location(locationDto)
                .categories(Set.of(categoryDto))
                .build();

        // -------- EVENT ENTITY (as it exists in DB) --------
        eventEntity = EventEntity.builder()
                .id(100L)
                .title("Rock Festival")
                .description("Annual rock music festival")
                .date(LocalDateTime.of(2025, 10, 20, 18, 0))
                .location(locationEntity)
                .categories(Set.of(categoryEntity))
                .deleted(false)
                .build();

        // -------- EVENT UPDATE REQUEST (used for update tests) --------
        eventUpdateRequest = EventUpdateRequest.builder()
                .title("Updated Rock Festival")
                .description("Updated description")
                .date(LocalDateTime.of(2025, 11, 5, 19, 0))
                .location(locationDto)
                .categories(Set.of(categoryDto))
                .build();

        // -------- RESPONSE DTO --------
        eventResponse = EventResponse.builder()
                .id(100L)
                .title("Rock Festival")
                .description("Annual rock music festival")
                .date(LocalDateTime.of(2025, 10, 20, 18, 0))
                .location(locationDto)
                .categories(Set.of(categoryDto))
                .thumbnailUrl(null)
                .build();
    }

    @Test
    void getEvents_shouldReturnMappedList() {
        when(eventRepository.findAllWithDetails()).thenReturn(List.of(eventEntity));
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        List<EventResponse> result = eventService.getEvents();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(eventResponse.getId());
        verify(eventRepository).findAllWithDetails();
        verify(eventMapper).toResponseDto(eventEntity);
    }

    @Test
    void myEvents_shouldReturnMappedListForOrganizer() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        when(eventRepository.findByOrganizerWithDetails(organizer)).thenReturn(List.of(eventEntity));
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        List<EventResponse> result = eventService.myEvents(organizer);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(eventResponse.getId());
        verify(eventRepository).findByOrganizerWithDetails(organizer);
        verify(eventMapper).toResponseDto(eventEntity);
    }

    @Test
    void getEventById_shouldReturnMappedEntity_whenFound() {
        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        EventResponse result = eventService.getEventById(eventEntity.getId());

        assertThat(result.getId()).isEqualTo(eventResponse.getId());
        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventMapper).toResponseDto(eventEntity);
    }

    @Test
    void getEventById_shouldThrow_whenNotFound() {
        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId() + 10))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(eventEntity.getId() + 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void createEvent_shouldMapAndSave() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        when(eventMapper.toEntity(eventRequest)).thenReturn(eventEntity);
        when(categoryRepository.findByName(categoryDto.getName())).thenReturn(Optional.of(categoryEntity));
        when(locationRepository.findByName(locationDto.getName())).thenReturn(Optional.empty());
        when(locationRepository.save(eventEntity.getLocation())).thenReturn(locationEntity);
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        EventResponse result = eventService.createEvent(eventRequest, organizer);

        assertThat(result.getId()).isEqualTo(eventResponse.getId());
        assertThat(eventEntity.getOrganizer()).isEqualTo(organizer);
        assertThat(eventEntity.getCategories()).contains(categoryEntity);
        assertThat(eventEntity.getLocation()).isEqualTo(locationEntity);
        verify(eventMapper).toEntity(eventRequest);
        verify(categoryRepository).findByName(categoryDto.getName());
        verify(locationRepository).findByName(locationDto.getName());
        verify(locationRepository).save(eventEntity.getLocation());
        verify(eventRepository).save(eventEntity);
        verify(eventMapper).toResponseDto(eventEntity);
    }

    @Test
    void createEvent_shouldThrow_whenCategoryNotFound() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        when(eventMapper.toEntity(eventRequest)).thenReturn(eventEntity);
        when(categoryRepository.findByName(categoryDto.getName())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(eventRequest, organizer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(eventMapper).toEntity(eventRequest);
        verify(categoryRepository).findByName(categoryDto.getName());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_shouldSoftDelete_whenOrganizerIsAllowed() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);

        MessageResponse result = eventService.deleteEvent(eventEntity.getId(), organizer);

        assertThat(eventEntity.isDeleted()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Event deleted");
        verify(eventRepository).save(eventEntity);
        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
    }

    @Test
    void deleteEvent_shouldThrow_whenEventNotFound() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId() + 10))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(eventEntity.getId() + 10, organizer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("event not found");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId() + 10);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_shouldThrow_whenUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.ORGANIZER);

        User originalOrganizer = new User();
        originalOrganizer.setId(1L);
        originalOrganizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(originalOrganizer);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));

        assertThatThrownBy(() -> eventService.deleteEvent(eventEntity.getId(), anotherUser))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("You are not allowed to delete this event");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_shouldUpdateFields_whenOrganizerIsAllowed() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(locationRepository.findByName(locationDto.getName())).thenReturn(Optional.of(locationEntity));
        when(categoryRepository.findByName(categoryDto.getName())).thenReturn(Optional.of(categoryEntity));
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        EventResponse result = eventService.updateEvent(eventEntity.getId(), eventUpdateRequest, organizer);

        assertThat(result).isEqualTo(eventResponse);
        assertThat(eventEntity.getTitle()).isEqualTo(eventUpdateRequest.getTitle());
        assertThat(eventEntity.getDescription()).isEqualTo(eventUpdateRequest.getDescription());
        assertThat(eventEntity.getDate()).isEqualTo(eventUpdateRequest.getDate());

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository).save(eventEntity);
        verify(eventMapper).toResponseDto(eventEntity);
    }

    @Test
    void updateEvent_shouldThrow_whenEventNotFound() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventEntity.getId(), eventUpdateRequest, organizer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("event not found");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_shouldThrow_whenUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.ORGANIZER);

        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));

        assertThatThrownBy(() -> eventService.updateEvent(eventEntity.getId(), eventUpdateRequest, anotherUser))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("You are not allowed to perform update");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_shouldUpdateLocation_whenProvided() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        LocationEntity newLocation = LocationEntity.builder()
                .id(2L)
                .name("New Hall")
                .address("New Street 5")
                .city("Kutaisi")
                .latitude(42.25)
                .longitude(42.70)
                .build();

        LocationDto newLocationDto = LocationDto.builder()
                .name("New Hall")
                .address("New Street 5")
                .city("Kutaisi")
                .latitude(42.25)
                .longitude(42.70)
                .build();

        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .location(newLocationDto)
                .build();

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(locationRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(locationMapper.mapFrom(newLocationDto)).thenReturn(newLocation);
        when(locationRepository.save(newLocation)).thenReturn(newLocation);
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        EventResponse result = eventService.updateEvent(eventEntity.getId(), updateRequest, organizer);

        assertThat(result).isEqualTo(eventResponse);
        assertThat(eventEntity.getLocation()).isEqualTo(newLocation);

        verify(locationRepository).findByName(newLocationDto.getName());
        verify(locationMapper).mapFrom(any(LocationDto.class));
        verify(locationRepository).save(newLocation);
        verify(eventRepository).save(eventEntity);
    }


    @Test
    void updateEvent_shouldUpdateCategories_whenProvided() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        CategoryDto newCategoryDto = CategoryDto.builder().name("Sports").build();
        CategoryEntity newCategoryEntity = CategoryEntity.builder().id(2L).name("Sports").build();

        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .categories(Set.of(newCategoryDto))
                .build();

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(categoryRepository.findByName("Sports")).thenReturn(Optional.of(newCategoryEntity));
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(eventResponse);

        EventResponse result = eventService.updateEvent(eventEntity.getId(), updateRequest, organizer);

        assertThat(result).isEqualTo(eventResponse);
        assertThat(eventEntity.getCategories()).contains(newCategoryEntity);

        verify(categoryRepository).findByName("Sports");
        verify(eventRepository).save(eventEntity);
    }

    @Test
    void uploadThumbnail_shouldSaveFileAndReturnUrl_whenOrganizerIsAllowed() throws Exception {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "dummy".getBytes());

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));
        when(eventRepository.save(eventEntity)).thenReturn(eventEntity);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(CopyOption.class)))
                    .thenReturn(1L);

            MessageResponse result = eventService.uploadThumbnail(eventEntity.getId(), file, organizer);

            assertThat(result.getMessage()).contains("/events/" + eventEntity.getId() + "/");
            assertThat(eventEntity.getThumbnailUrl()).contains("/events/" + eventEntity.getId() + "/");

            verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
            verify(eventRepository).save(eventEntity);
        }
    }


    @Test
    void uploadThumbnail_shouldThrow_whenFileIsEmpty() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));

        assertThatThrownBy(() -> eventService.uploadThumbnail(eventEntity.getId(), emptyFile, organizer))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("File is empty");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void uploadThumbnail_shouldThrow_whenEventNotFound() {
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "dummy".getBytes());

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.uploadThumbnail(eventEntity.getId(), file, organizer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void uploadThumbnail_shouldThrow_whenUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.ORGANIZER);

        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);
        eventEntity.setOrganizer(organizer);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "dummy".getBytes());

        when(eventRepository.findByIdAndDeletedFalse(eventEntity.getId()))
                .thenReturn(Optional.of(eventEntity));

        assertThatThrownBy(() -> eventService.uploadThumbnail(eventEntity.getId(), file, anotherUser))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("You are not allowed to update this event");

        verify(eventRepository).findByIdAndDeletedFalse(eventEntity.getId());
        verify(eventRepository, never()).save(any());
    }
}
