package ge.ticketebi.ticketebi_backend.security;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.AuthTokensDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.LoginRequestDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.RefreshTokenRequestDto;
import ge.ticketebi.ticketebi_backend.domain.dto.auth.RegisterRequestDto;
import ge.ticketebi.ticketebi_backend.domain.entities.User;

public interface AuthService {
    MessageResponse register(RegisterRequestDto request);
    MessageResponse registerAsOrganizer(RegisterRequestDto request);
    AuthTokensDto login(LoginRequestDto request);
    void logout (RefreshTokenRequestDto request, String username);
    AuthTokensDto refreshToken(RefreshTokenRequestDto request);
    AuthTokensDto issueTokens(User user);
}
