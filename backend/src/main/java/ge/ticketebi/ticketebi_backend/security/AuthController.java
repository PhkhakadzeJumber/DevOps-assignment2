package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.*;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.security.verification.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    @Value("${app.security.jwt.refresh-ttl-days}") private long refreshTtlDays;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody @Valid RegisterRequestDto request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register-organizer")
    public ResponseEntity<MessageResponse> registerAsOrganizer(@RequestBody  @Valid RegisterRequestDto request) {
        MessageResponse response = authService.registerAsOrganizer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(@RequestBody LoginRequestDto request) {
        AuthTokensDto loginResult = authService.login(request);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(false)  // set to true in production
                .path("/api/auth")
                .maxAge(TimeUnit.DAYS.toSeconds(refreshTtlDays))
                .sameSite("Strict")
                .build();
        AccessTokenResponseDto response = new AccessTokenResponseDto(loginResult.getAccessToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (refreshToken != null) {
            RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();
            refreshTokenRequestDto.setRefreshToken(refreshToken);
            authService.logout(refreshTokenRequestDto, authentication.getName());
        }

        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // set to true in production
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(new MessageResponse("Logged out successfully"));
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponseDto> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new UnauthorizedActionException("Refresh token not found or expired");
        }

        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();
        refreshTokenRequestDto.setRefreshToken(refreshToken);

        AuthTokensDto tokens = authService.refreshToken(refreshTokenRequestDto);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)  // set to true in production
                .path("/api/auth")
                .maxAge(TimeUnit.DAYS.toSeconds(refreshTtlDays))
                .sameSite("Strict")
                .build();

        AccessTokenResponseDto response = new AccessTokenResponseDto(tokens.getAccessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam String email) {
        verificationService.sendVerification(email);
        return ResponseEntity.ok(new MessageResponse("Verification email resent"));
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verify(@RequestParam String token) {
        verificationService.verify(token);
        return ResponseEntity.ok(new MessageResponse("Email verified. You can now log in."));
    }
}