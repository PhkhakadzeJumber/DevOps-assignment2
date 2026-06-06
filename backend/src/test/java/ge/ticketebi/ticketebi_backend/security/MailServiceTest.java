package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.security.verification.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendVerificationEmail_shouldSendEmailWithCorrectContent() {
        String toEmail = "test@mail.com";
        String link = "http://localhost:8080/api/auth/verify?token=abc123";

        mailService.sendVerificationEmail(toEmail, link);

        verify(mailSender).send(org.mockito.ArgumentMatchers.argThat((SimpleMailMessage msg) ->
                Objects.requireNonNull(msg.getTo())[0].equals(toEmail) &&
                        Objects.equals(msg.getSubject(), "Verify your email") &&
                        Objects.requireNonNull(msg.getText()).contains(link)
        ));
    }
}
