package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.auth.RegisterRequestDto;
import ge.ticketebi.ticketebi_backend.domain.entities.AuthProvider;
import ge.ticketebi.ticketebi_backend.domain.entities.Role;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class AuthMapperImpl implements Mapper<User, RegisterRequestDto> {

    /**
     * Map from RegisterRequestDto to User entity.
     * Note: password encoding and role assignment should be done in the service.
     */
    @Override
    public User mapFrom(RegisterRequestDto dto) {
        if (dto == null) return null;

        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword()) // encode in service
                .enabled(false)
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    @Override
    public RegisterRequestDto mapTo(User user) {
        if (user == null) return null;

        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // no password mapping
        return dto;
    }

}