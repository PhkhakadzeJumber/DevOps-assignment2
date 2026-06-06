package ge.ticketebi.ticketebi_backend.security.qr;

import ge.ticketebi.ticketebi_backend.domain.entities.TicketEntity;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidQrTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class QrTokenService {

    @Value("${app.security.qr.secret}") private String secret;
    @Value("${app.security.qr.issuer}") private String issuer;
    @Value("${app.security.qr.ttl-days}") private long defaultTtlDays;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateForTicket(TicketEntity ticket) {
        Instant now = Instant.now();
        Instant exp = resolveExpiration(ticket, now);

        return Jwts.builder()
                .subject(ticket.getTicketNumber())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "TICKET_QR")
                .claim("eventId", ticket.getTicketType().getEvent().getId())
                .id(UUID.randomUUID().toString())
                .signWith(key())
                .compact();
    }

    public String extractTicketNumber(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidQrTokenException("Invalid QR token", ex);
        }

        if (!issuer.equals(claims.getIssuer())) {
            throw new InvalidQrTokenException("Invalid QR token issuer");
        }

        Object type = claims.get("type");
        if (!"TICKET_QR".equals(type)) {
            throw new InvalidQrTokenException("Invalid QR token type");
        }

        return claims.getSubject();
    }

    private Instant resolveExpiration(TicketEntity ticket, Instant now) {
        if (ticket.getTicketType() == null || ticket.getTicketType().getEvent() == null || ticket.getTicketType().getEvent().getDate() == null) {
            return now.plus(Duration.ofDays(defaultTtlDays));
        }

        Instant eventInstant = ticket.getTicketType().getEvent().getDate()
                .atZone(ZoneId.systemDefault())
                .toInstant();
        Instant minExp = now.plus(Duration.ofDays(1));
        Instant eventBasedExp = eventInstant.plus(Duration.ofDays(2));
        return eventBasedExp.isAfter(minExp) ? eventBasedExp : minExp;
    }
}
