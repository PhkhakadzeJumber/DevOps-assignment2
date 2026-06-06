package ge.ticketebi.ticketebi_backend.security.oauth2;

import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.AuthTokensDto;
import ge.ticketebi.ticketebi_backend.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    @Value("${app.security.jwt.refresh-ttl-days}") private long refreshTtlDays;
    @Value("${app.oauth2.redirect-url}") private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOidcUserPrincipal principal = (CustomOidcUserPrincipal) authentication.getPrincipal();

        User user = principal.getUser();

        AuthTokensDto tokens = authService.issueTokens(user);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false) // set to true in production
                .path("/api/auth")
                .maxAge(TimeUnit.DAYS.toSeconds(refreshTtlDays))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        clearAuthenticationAttributes(request);
    }
}
