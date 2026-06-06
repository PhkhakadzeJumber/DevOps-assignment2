package ge.ticketebi.ticketebi_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDto {

    @NotBlank(message = "location name is required")
    private String name;

    @NotBlank(message = "address is required")
    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotNull(message = "latitude is required")
    private Double latitude;

    @NotNull(message = "longitude is required")
    private Double longitude;
}
