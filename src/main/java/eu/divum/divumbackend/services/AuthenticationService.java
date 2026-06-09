package eu.divum.divumbackend.services;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    boolean validatePassword(String rawPassword, String hashedPassword);
}
