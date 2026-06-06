package ge.ticketebi.ticketebi_backend.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokensDto {
    private String accessToken;
    private String refreshToken;
}
