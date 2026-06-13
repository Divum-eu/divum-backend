package eu.divum.divumbackend.exceptions.jwt;

public class JwtSigningContextGenerationError extends RuntimeException {
    public JwtSigningContextGenerationError(String message) {
        super(message);
    }
}
