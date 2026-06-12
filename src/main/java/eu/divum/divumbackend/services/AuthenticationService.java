package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.auth.*;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    boolean validatePassword(String rawPassword, String hashedPassword);

    AuthenticatedDto login(LoginUserRequest request);

    AuthenticatedDto register(RegisterUserRequest request);
}
