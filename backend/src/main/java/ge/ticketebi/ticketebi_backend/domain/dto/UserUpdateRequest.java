package ge.ticketebi.ticketebi_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    private String username;

    @Pattern(regexp = "^\\+\\d{12}$", message = "Phone must be + followed by exactly 12 digits")
    private String phoneNumber;

}
