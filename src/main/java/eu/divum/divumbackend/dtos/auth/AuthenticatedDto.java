package eu.divum.divumbackend.dtos.auth;

public record AuthenticatedDto(
        String userId,
        String accessToken,
        String refreshToken
) {
}
