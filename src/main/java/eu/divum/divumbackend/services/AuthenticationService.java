package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.auth.JwtResponse;
import eu.divum.divumbackend.dtos.auth.LoginUserRequest;
import eu.divum.divumbackend.dtos.auth.RegisterDto;
import eu.divum.divumbackend.dtos.auth.RegisterUserRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    boolean validatePassword(String rawPassword, String hashedPassword);

    JwtResponse login(LoginUserRequest request, HttpServletResponse response);

    RegisterDto register(RegisterUserRequest request, HttpServletResponse response);
}
