package ge.ticketebi.ticketebi_backend.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8–64 characters")
    @Pattern(
            regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$",
            message = "Password must contain upper, lower and digit"
    )
    private String password;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    private String username;
}
