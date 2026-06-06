package ge.ticketebi.ticketebi_backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${app.security.jwt.secret}") private String secret;
    @Value("${app.security.jwt.issuer}") private String issuer;
    @Value("${app.security.jwt.access-ttl-min}") private long accessTtlMin;
    @Value("${app.security.jwt.refresh-ttl-days}") private long refreshTtlDays;

    private SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(accessTtlMin))))
                .claim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofDays(refreshTtlDays))))
                .signWith(key())
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    public String getUsername(String token) { return parse(token).getPayload().getSubject(); }
    public boolean isExpired(String token) { return parse(token).getPayload().getExpiration().before(new Date()); }
}
