package eu.divum.divumbackend.exceptions.jwt;

public class JwtWriteError extends RuntimeException {
    public JwtWriteError(String message) {
        super(message);
    }
}
