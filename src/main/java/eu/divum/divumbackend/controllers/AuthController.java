package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.auth.JwtResponse;
import eu.divum.divumbackend.dtos.auth.LoginUserRequest;
import eu.divum.divumbackend.dtos.auth.RegisterDto;
import eu.divum.divumbackend.dtos.auth.RegisterUserRequest;
import eu.divum.divumbackend.services.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@RestController
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginUserRequest request,
            HttpServletResponse response
            ) {
        return ResponseEntity.ok(authenticationService.login(request, response));
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletResponse response,
            UriComponentsBuilder uriBuilder
    ) {
        RegisterDto registerDto = authenticationService.register(request, response);

        URI uri = uriBuilder.path("/v1/users/{id}").buildAndExpand(registerDto.userId()).toUri();

        return ResponseEntity.created(uri).body(new JwtResponse(registerDto.token()));
    }
}
