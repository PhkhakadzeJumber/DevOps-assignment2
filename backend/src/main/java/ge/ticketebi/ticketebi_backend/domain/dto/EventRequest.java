package ge.ticketebi.ticketebi_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EventRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @FutureOrPresent(message = "Event date must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime date;

    private Set<CategoryDto> categories = new HashSet<>();

    @NotNull(message = "Location is required")
    @Valid
    private LocationDto location;
}
