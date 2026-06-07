package eu.divum.divumbackend.services;

public interface AuthenticationService {
    String generatePasswordHash(String password);
}
