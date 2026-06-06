package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.domain.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteAllByUser(User user);
}