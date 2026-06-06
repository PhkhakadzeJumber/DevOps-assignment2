package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.UserDto;
import ge.ticketebi.ticketebi_backend.domain.dto.UserUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.UserRepository;
import ge.ticketebi.ticketebi_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Mapper<User, UserDto> userMapper;

    @Override
    public UserDto me(User user) {
        return userMapper.mapTo(user);
    }

    @Override
    public UserDto updateUser(UserUpdateRequest userUpdateRequest, User user) {
        if(userRepository.existsByUsername(userUpdateRequest.getUsername()))
            throw new InvalidRequestException("username already exists");

        if(userUpdateRequest.getPhoneNumber() != null)
            user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        if(userUpdateRequest.getUsername() != null)
            user.setUsername(userUpdateRequest.getUsername());

        userRepository.save(user);
        return userMapper.mapTo(user);
    }

    @Override
    public MessageResponse deleteUser(User user) {
        userRepository.delete(user);
        return new MessageResponse("user deleted");
    }

}
