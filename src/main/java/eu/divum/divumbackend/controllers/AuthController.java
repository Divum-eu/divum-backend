package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.config.JwtConfig;
import eu.divum.divumbackend.dtos.auth.*;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@RestController
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginUserRequest request,
            HttpServletResponse response
            ) {

        AuthenticatedDto authenticatedDto = authenticationService.login(request);

        attachRefreshTokenCookie(response, authenticatedDto.refreshToken());

        return ResponseEntity.ok(new JwtResponse(authenticatedDto.accessToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletResponse response,
            UriComponentsBuilder uriBuilder
    ) {
        AuthenticatedDto authenticatedDto = authenticationService.register(request);

        URI uri = uriBuilder.path("/v1/users/{id}").buildAndExpand(authenticatedDto.userId()).toUri();

        attachRefreshTokenCookie(response, authenticatedDto.refreshToken());

        return ResponseEntity.created(uri).body(new JwtResponse(authenticatedDto.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue("refreshToken") String refreshToken
    ) {
        AuthenticatedDto authenticatedDto = authenticationService.refresh(refreshToken);
        return ResponseEntity.accepted().body(new JwtResponse(authenticatedDto.accessToken()));
    }

    private void attachRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
