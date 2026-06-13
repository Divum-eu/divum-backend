package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.config.JwtConfig;
import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.dtos.auth.*;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.exceptions.user.EmailTaken;
import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.user.UsernameTaken;
import eu.divum.divumbackend.mappers.user.UserMapper;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.security.Jwt;
import eu.divum.divumbackend.security.JwtService;
import eu.divum.divumbackend.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;

    @Override
    public String generatePasswordHash(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public AuthenticatedDto login(LoginUserRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(request.username()).orElseThrow(
                () -> new UserNotFound("User not found.")
        );

        Jwt accessToken = jwtService.generateAccessToken(user);
        Jwt refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticatedDto(user.getId().toString(), accessToken.toString(), refreshToken.toString());
    }

    @Override
    public AuthenticatedDto register(RegisterUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTaken("Username " + request.username() + " is taken.");
        }
        if (userRepository.existsByEmailAddress(request.email())) {
            throw new EmailTaken("Email is already registered.");
        }

        User user = userMapper.mapToEntity(
                new CreateUserRequest(
                        request.username(),
                        request.email(),
                        request.password()
                )
        );
        user.setPasswordData(generatePasswordHash(request.password()));
        userRepository.save(user);

        Jwt accessToken = jwtService.generateAccessToken(user);
        Jwt refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticatedDto(user.getId().toString(), accessToken.toString(), refreshToken.toString());
    }
}
