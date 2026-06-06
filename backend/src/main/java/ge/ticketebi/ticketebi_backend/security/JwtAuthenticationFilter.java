package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.config.PublicEndpointsConfig;
import ge.ticketebi.ticketebi_backend.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService uds;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                if (!jwt.isExpired(token)) {
                    String username = jwt.getUsername(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails user = uds.loadUserByUsername(username);
                        var at = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        at.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(at);
                    }
                }
            } catch (JwtException ignored) {  }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(PublicEndpointsConfig.PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }
}
