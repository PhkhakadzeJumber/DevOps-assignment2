package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.UserDto;
import ge.ticketebi.ticketebi_backend.domain.dto.UserUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.entities.User;

import java.util.List;

public interface UserService {
    UserDto me(User user);
    UserDto updateUser(UserUpdateRequest userUpdateRequest, User user);
    MessageResponse deleteUser(User user);
}
