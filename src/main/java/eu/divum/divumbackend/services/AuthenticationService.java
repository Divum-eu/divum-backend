package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.auth.AuthenticatedDto;
import eu.divum.divumbackend.dtos.auth.LoginUserRequest;
import eu.divum.divumbackend.dtos.auth.RegisterUserRequest;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    boolean validatePassword(String rawPassword, String hashedPassword);

    AuthenticatedDto login(LoginUserRequest request);

    AuthenticatedDto register(RegisterUserRequest request);

    AuthenticatedDto refresh(String refreshToken);
}
