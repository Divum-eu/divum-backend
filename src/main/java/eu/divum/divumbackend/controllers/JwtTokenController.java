package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.services.JwtTokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwtTokenController {

    private final JwtTokenService jwtTokenService;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return jwtTokenService.getPublicKeys();
    }
}