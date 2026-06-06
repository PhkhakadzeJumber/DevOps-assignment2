package ge.ticketebi.ticketebi_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    private String username;

    private String email;

    @Pattern(regexp = "^\\+\\d{12}$", message = "Phone must be + followed by exactly 12 digits")
    private String phoneNumber;

    private Role role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
