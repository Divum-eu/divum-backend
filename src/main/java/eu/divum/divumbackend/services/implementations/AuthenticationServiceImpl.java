package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.dtos.auth.AuthenticatedDto;
import eu.divum.divumbackend.dtos.auth.LoginUserRequest;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.exceptions.user.EmailTaken;
import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.user.UsernameTaken;
import eu.divum.divumbackend.mappers.user.UserMapper;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.security.UserJwt;
import eu.divum.divumbackend.security.UserJwtService;
import eu.divum.divumbackend.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserJwtService userJwtService;
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

        UserJwt accessToken = userJwtService.generateAccessToken(user);
        UserJwt refreshToken = userJwtService.generateRefreshToken(user);

        return new AuthenticatedDto(user.getId().toString(), accessToken.toString(), refreshToken.toString());
    }

    @Override
    public AuthenticatedDto register(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTaken("Username " + request.username() + " is taken.");
        }
        if (userRepository.existsByEmailAddress(request.emailAddress())) {
            throw new EmailTaken("Email is already registered.");
        }

        User user = userMapper.mapToEntity(
                new CreateUserRequest(
                        request.username(),
                        request.emailAddress(),
                        request.password()
                )
        );
        user.setPasswordData(generatePasswordHash(request.password()));
        userRepository.save(user);

        UserJwt accessToken = userJwtService.generateAccessToken(user);
        UserJwt refreshToken = userJwtService.generateRefreshToken(user);

        return new AuthenticatedDto(user.getId().toString(), accessToken.toString(), refreshToken.toString());
    }

    @Override
    public AuthenticatedDto refresh(String refreshToken) {

        UUID userId = userJwtService.parseToken(refreshToken).getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found."));

        if (userJwtService.parseToken(refreshToken).isExpired()) {
            throw new BadCredentialsException("Invalid or expired refresh token.");
        }

        UserJwt newAccessToken = userJwtService.generateAccessToken(user);

        return new AuthenticatedDto(userId.toString(), newAccessToken.toString(), refreshToken);
    }
}
