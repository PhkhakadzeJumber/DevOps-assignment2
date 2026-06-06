package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.domain.entities.VerificationToken;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import ge.ticketebi.ticketebi_backend.repositories.VerificationTokenRepository;
import ge.ticketebi.ticketebi_backend.security.verification.MailService;
import ge.ticketebi.ticketebi_backend.security.verification.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private MailService mailService;

    @InjectMocks private VerificationService verificationService;

    private User user;
    private VerificationToken tokenEntity;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setUsername("testUser");
        user.setEnabled(false);

        tokenEntity = new VerificationToken();
        tokenEntity.setToken("verificationToken");
        tokenEntity.setUser(user);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(3600));
    }

    @Test
    void generateAndSendToken_shouldCreateTokenAndSendEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.ofNullable(user));
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        verificationService.sendVerification(user.getEmail());

        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(mailService).sendVerificationEmail(eq(user.getEmail()), anyString());
    }

    @Test
    void verifyToken_shouldEnableUserAndMarkTokenUsed() {
        when(verificationTokenRepository.findByToken("verificationToken"))
                .thenReturn(Optional.of(tokenEntity));
        when(userRepository.save(user)).thenReturn(user);
        when(verificationTokenRepository.save(tokenEntity)).thenReturn(tokenEntity);

        verificationService.verify("verificationToken");

        assertThat(user.isEnabled()).isTrue();
        assertThat(tokenEntity.getUsedAt()).isNotNull();

        verify(userRepository).save(user);
        verify(verificationTokenRepository).save(tokenEntity);
    }

    @Test
    void verifyToken_shouldThrow_whenTokenNotFound() {
        when(verificationTokenRepository.findByToken("invalidToken"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.verify("invalidToken"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid token");

        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyToken_shouldThrow_whenTokenAlreadyUsed() {
        tokenEntity.setUsedAt(LocalDateTime.now());
        when(verificationTokenRepository.findByToken("verificationToken"))
                .thenReturn(Optional.of(tokenEntity));

        assertThatThrownBy(() -> verificationService.verify("verificationToken"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Token already used");

        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyToken_shouldThrow_whenTokenExpired() {
        tokenEntity.setExpiresAt(LocalDateTime.now().minusSeconds(10));
        when(verificationTokenRepository.findByToken("verificationToken"))
                .thenReturn(Optional.of(tokenEntity));

        assertThatThrownBy(() -> verificationService.verify("verificationToken"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Token expired");

        verifyNoInteractions(userRepository);
    }
}
