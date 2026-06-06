package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.UserDto;
import ge.ticketebi.ticketebi_backend.domain.dto.UserUpdateRequest;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import ge.ticketebi.ticketebi_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.me(user));
    }

    @PatchMapping("/update")
    public ResponseEntity<UserDto> update(
            @RequestBody @Valid UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal User user
            ) {
        return ResponseEntity.ok(userService.updateUser(userUpdateRequest, user));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<MessageResponse> delete(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.deleteUser(user));
    }


}
