package ge.ticketebi.ticketebi_backend.security.oauth2;

import ge.ticketebi.ticketebi_backend.domain.entities.AuthProvider;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String sub = oidcUser.getAttribute("sub");
        String email = oidcUser.getAttribute("email");

        User user = userRepository.findByProviderId(sub)
                .orElseGet(() -> {
                    User existingUser = userRepository.findByEmail(email).orElse(null);
                    if (existingUser != null) {
                        existingUser.setAuthProvider(AuthProvider.GOOGLE);
                        existingUser.setProviderId(sub);
                        if (!existingUser.isEnabled()) existingUser.setEnabled(true);
                        return userRepository.save(existingUser);
                    } else {
                        User newUser = User.builder()
                                .username(email)
                                .email(email)
                                .password(null)
                                .enabled(true)
                                .role(Role.CUSTOMER)
                                .authProvider(AuthProvider.GOOGLE)
                                .providerId(sub)
                                .build();
                        return userRepository.save(newUser);
                    }
                });

        return new CustomOidcUserPrincipal(oidcUser, user);
    }
}