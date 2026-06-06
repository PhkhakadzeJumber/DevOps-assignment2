package ge.ticketebi.ticketebi_backend.security.verification;

import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.domain.entities.VerificationToken;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import ge.ticketebi.ticketebi_backend.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationService {
    private final VerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final MailService mailService;

    public void sendVerification(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new InvalidRequestException("User already verified.");
        }

        tokenRepo.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenRepo.save(vt);

        String link = "http://localhost:8080/api/auth/verify?token=" + token;
        mailService.sendVerificationEmail(user.getEmail(), link);
    }

    public void verify(String token) {
        VerificationToken vt = tokenRepo.findByToken(token)
                .orElseThrow(() -> new InvalidRequestException("Invalid token"));

        if (vt.getUsedAt() != null) {
            throw new InvalidRequestException("Token already used");
        }

        if (vt.getExpiresAt().isBefore(LocalDateTime.now()) ) {
            throw new InvalidRequestException("Token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepo.save(user);

        vt.setUsedAt(LocalDateTime.now());
        tokenRepo.save(vt);
    }
}