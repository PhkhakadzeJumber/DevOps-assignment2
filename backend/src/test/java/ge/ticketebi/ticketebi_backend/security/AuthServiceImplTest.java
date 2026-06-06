package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.AuthTokensDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.LoginRequestDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.RefreshTokenRequestDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.RegisterRequestDto;
import ge.ticketebi.ticketebi_backend.domain.entities.RefreshToken;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.RefreshTokenRepository;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import ge.ticketebi.ticketebi_backend.security.jwt.JwtService;
import ge.ticketebi.ticketebi_backend.security.verification.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Mapper<User, RegisterRequestDto> userMapper;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;
    @Mock private VerificationService verificationService;

    @InjectMocks private AuthServiceImpl authService;

    private User customerEntity;
    private User organiserEntity;
    private RefreshToken refreshTokenEntity;
    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private RefreshTokenRequestDto refreshTokenRequest;

    @BeforeEach
    void setup(){
        customerEntity = new User();
        customerEntity.setEmail("test@mail.com");
        customerEntity.setUsername("testUser");
        customerEntity.setPassword("testPassword");
        customerEntity.setEnabled(true);
        customerEntity.setRole(Role.CUSTOMER);

        organiserEntity = new User();
        organiserEntity.setEmail("test@mail.com");
        organiserEntity.setUsername("testUser");
        organiserEntity.setPassword("testPassword");
        organiserEntity.setEnabled(true);
        organiserEntity.setRole(Role.ORGANIZER);

        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@mail.com");
        registerRequest.setUsername("testUser");
        registerRequest.setPassword("testPassword");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@mail.com");
        loginRequest.setPassword("testPassword");

        refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("someRefreshToken");

        refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken("someRefreshToken");
        refreshTokenEntity.setUser(customerEntity);
        refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(60 * 60));
        refreshTokenEntity.setRevoked(false);
    }

    @Test
    void register_shouldSendVerificationEmail() {
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userMapper.mapFrom(registerRequest)).thenReturn(customerEntity);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(customerEntity);

        doNothing().when(verificationService).sendVerification(customerEntity.getEmail());

        MessageResponse result = authService.register(registerRequest);

        verify(userRepository).save(customerEntity);
        verify(verificationService).sendVerification(customerEntity.getEmail());

        assertThat(result.getMessage()).isEqualTo("Registration successful. Please verify your email.");
    }


    @Test
    void registerAsOrganiser_shouldSendVerificationEmail(){
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userMapper.mapFrom(registerRequest)).thenReturn(organiserEntity);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(organiserEntity);

        doNothing().when(verificationService).sendVerification(organiserEntity.getEmail());

        MessageResponse result = authService.register(registerRequest);

        verify(userRepository).save(organiserEntity);
        verify(verificationService).sendVerification(organiserEntity.getEmail());

        assertThat(result.getMessage()).isEqualTo("Registration successful. Please verify your email.");
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Email already in use");

        assertThatThrownBy(() -> authService.registerAsOrganizer(registerRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void register_shouldThrow_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Username already taken");

        assertThatThrownBy(() -> authService.registerAsOrganizer(registerRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Username already taken");
    }

    @Test
    void login_shouldReturnAuthResponse() {
        when(authentication.getPrincipal()).thenReturn(customerEntity);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(customerEntity)).thenReturn("someAccessToken");
        when(jwtService.generateRefreshToken(customerEntity)).thenReturn("someRefreshToken");

        AuthTokensDto result = authService.login(loginRequest);

        assertThat(result.getAccessToken()).isEqualTo("someAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("someRefreshToken");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(customerEntity);
        verify(jwtService).generateRefreshToken(customerEntity);
        verify(refreshTokenRepository).revokeAllForUser(customerEntity.getId());
    }

    @Test
    void login_shouldThrowException_whenUserNotEnabled() {
        customerEntity.setEnabled(false);

        when(authentication.getPrincipal()).thenReturn(customerEntity);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Please verify your email before logging in");

        verifyNoInteractions(jwtService, refreshTokenRepository);
    }


    @Test
    void login_shouldThrowException_whenAuthenticationFails() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        try {
            authService.login(loginRequest);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Bad credentials");
        }

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    void logout_shouldRevokeToken() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.of(refreshTokenEntity));
        when(refreshTokenRepository.save(refreshTokenEntity)).thenReturn(refreshTokenEntity);

        authService.logout(refreshTokenRequest, "testUser");

        assertThat(refreshTokenEntity.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshTokenEntity);
    }

    @Test
    void logout_shouldThrowInvalidRequestException_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(refreshTokenRequest, "testUser"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Token not found or revoked");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_shouldThrowUnauthorizedActionException_whenTokenBelongsToDifferentUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        refreshTokenEntity.setUser(otherUser);

        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.of(refreshTokenEntity));

        assertThatThrownBy(() -> authService.logout(refreshTokenRequest, "testUser"))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessage("Token does not belong to user");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.of(refreshTokenEntity));
        when(jwtService.generateAccessToken(customerEntity)).thenReturn("newAccessToken");

        AuthTokensDto result = authService.refreshToken(refreshTokenRequest);

        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("someRefreshToken");

        verify(jwtService).generateAccessToken(customerEntity);
    }

    @Test
    void refreshToken_shouldThrowInvalidRequestException_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void refreshToken_shouldThrowInvalidRequestException_whenTokenExpired() {
        refreshTokenEntity.setExpiresAt(Instant.now().minusSeconds(10)); // expired
        when(refreshTokenRepository.findByTokenAndRevokedFalse("someRefreshToken"))
                .thenReturn(Optional.of(refreshTokenEntity));

        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Refresh token expired");
    }
}
