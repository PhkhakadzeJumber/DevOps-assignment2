package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "randomkeyhfiwehfiuoh1y981y391391");
        ReflectionTestUtils.setField(jwtService, "issuer", "ticketebi");
        ReflectionTestUtils.setField(jwtService, "accessTtlMin", 60L);
        ReflectionTestUtils.setField(jwtService, "refreshTtlDays", 7L);
    }

    @Test
    void generateAccessToken_shouldContainUsernameAndRoles() {
        UserDetails user = new User(
                "user123",
                "password",
                List.of(() -> "ROLE_CUSTOMER")
        );

        String token = jwtService.generateAccessToken(user);
        Jws<Claims> claims = jwtService.parse(token);

        assertThat(claims.getPayload().getSubject()).isEqualTo("user123");
        assertThat(claims.getPayload().getIssuer()).isEqualTo("ticketebi");
        assertThat(claims.getPayload().get("roles", List.class).contains("ROLE_CUSTOMER")).isTrue();
    }

    @Test
    void isExpired_shouldReturnFalseForNewToken() {
        UserDetails user = new org.springframework.security.core.userdetails.User(
                "user123", "password", List.of()
        );

        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isExpired(token)).isFalse();
    }

    @Test
    void getUsername_shouldReturnUsername() {
        UserDetails user = new org.springframework.security.core.userdetails.User(
                "user123", "password", List.of()
        );

        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.getUsername(token)).isEqualTo("user123");
    }
}
