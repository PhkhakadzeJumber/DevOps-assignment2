package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // load by username or email
        User user = users.findByUsername(identifier)
                .orElseGet(() -> users.findByEmail(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + identifier)));

        return user;
    }
}