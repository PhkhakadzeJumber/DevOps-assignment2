package ge.ticketebi.ticketebi_backend.security.verification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Value("${spring.mail.username}") private String email;

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String link) {
        String subject = "Verify your email";

        String body = "Hello,\n\nPlease click the link below to verify your email:\n"
                + link
                + "\n\nThis link will expire in 24 hours.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(email);

        mailSender.send(message);
    }
}
