package eu.divum.divumbackend.services;

import java.util.Map;

public interface JwtTokenService {
    String writeSignedToken(Map<String, String> claims, String issuer);

    Map<String, Object> getPublicKeys();
}