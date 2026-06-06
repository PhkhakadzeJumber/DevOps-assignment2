package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.UserDto;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements Mapper<User, UserDto> {

    @Override
    public UserDto mapTo(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .phoneNumber(userDto.getPhoneNumber())
                .role(userDto.getRole())
                .createdAt(userDto.getCreatedAt())
                .build();
    }

}
