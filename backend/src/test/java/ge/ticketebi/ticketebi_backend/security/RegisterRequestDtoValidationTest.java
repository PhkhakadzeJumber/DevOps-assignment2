package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.dto.auth.RegisterRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        dto.setUsername("user123");
        dto.setPassword(""); // empty password

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting("message")
                .contains("Password is required");
    }

    @Test
    void shouldFail_whenPasswordTooShort() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        dto.setUsername("user123");
        dto.setPassword("Ab1"); // too short

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting("message")
                .contains("Password must be between 8–64 characters");
    }

    @Test
    void shouldFail_whenPasswordWeak() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        dto.setUsername("user123");
        dto.setPassword("abcdefgh"); // no uppercase, no digit

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting("message")
                .contains("Password must contain upper, lower and digit");
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("not-an-email");
        dto.setUsername("user123");
        dto.setPassword("Abcdef1g");

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting("message")
                .contains("Invalid email format");
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        dto.setUsername("user123");
        dto.setPassword("Abcd1234"); // valid

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
