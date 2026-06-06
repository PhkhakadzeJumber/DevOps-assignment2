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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final Mapper<User, RegisterRequestDto> userMapper;
    private final VerificationService verificationService;
    @Value("${app.security.jwt.refresh-ttl-days}") private long refreshTtlDays;

    public MessageResponse register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidRequestException("Username already taken");
        }
        User user = userMapper.mapFrom(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        verificationService.sendVerification(request.getEmail());

        return new MessageResponse("Registration successful. Please verify your email.");
    }

    @Override
    public MessageResponse registerAsOrganizer(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidRequestException("Username already taken");
        }
        User user = userMapper.mapFrom(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ORGANIZER);

        userRepository.save(user);

        verificationService.sendVerification(request.getEmail());

        return new MessageResponse("Registration successful. Please verify your email.");
    }

    public AuthTokensDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();

        if (!user.isEnabled())
            throw new InvalidRequestException("Please verify your email before logging in");

        return issueTokens(user);
    }

    @Override
    public void logout(RefreshTokenRequestDto refreshToken, String username) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken.getRefreshToken())
                .orElseThrow(() -> new InvalidRequestException("Token not found or revoked"));

        if (!token.getUser().getUsername().equals(username)) {
            throw new UnauthorizedActionException("Token does not belong to user");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public AuthTokensDto refreshToken(RefreshTokenRequestDto request) {
        RefreshToken token = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRequestException("Invalid refresh token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRequestException("Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(token.getUser());
        return new AuthTokensDto(newAccessToken, token.getToken());
    }

    @Override
    public AuthTokensDto issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.revokeAllForUser(user.getId());
        saveRefreshToken(refreshToken, user);

        return new AuthTokensDto(accessToken, refreshToken);
    }

    private RefreshToken saveRefreshToken(String refreshToken, User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshTtlDays)))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }
}
