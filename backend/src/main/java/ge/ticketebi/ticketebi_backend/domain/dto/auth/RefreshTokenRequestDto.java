package ge.ticketebi.ticketebi_backend.domain.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    private String refreshToken;
}
