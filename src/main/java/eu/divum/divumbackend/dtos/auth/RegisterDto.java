package eu.divum.divumbackend.dtos.auth;

import java.util.UUID;

public record RegisterDto(String token, UUID userId) {
}
