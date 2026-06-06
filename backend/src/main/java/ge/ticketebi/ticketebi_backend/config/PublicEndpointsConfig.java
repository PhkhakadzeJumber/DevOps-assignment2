package ge.ticketebi.ticketebi_backend.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class PublicEndpointsConfig {
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/register-organizer",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/resend-verification",
            "/api/auth/verify",
            "/oauth2/**",
            "/login/oauth2/**",
            "/events/**", // thumbnails
            "/health",
            "/api/docs/**",
            "/api/swagger/**",
            "/api/swagger-ui/**"
    };
}
