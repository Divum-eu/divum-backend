package eu.divum.divumbackend.services;

public interface AuthenticationService {
    String generatePasswordHash(String password);

    Boolean validatePassword(String rawPassword, String hashedPassword);
}
