package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.auth.AuthenticatedDto;
import eu.divum.divumbackend.dtos.auth.LoginUserRequest;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    boolean validatePassword(String rawPassword, String hashedPassword);

    AuthenticatedDto login(LoginUserRequest request);

    AuthenticatedDto register(CreateUserRequest request);

    AuthenticatedDto refresh(String refreshToken);
}
