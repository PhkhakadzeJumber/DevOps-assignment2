package ge.ticketebi.ticketebi_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime date;

    private UserDto organizer;

    private Set<CategoryDto> categories = new HashSet<>();

    private LocationDto location;

    private String thumbnailUrl;
}
