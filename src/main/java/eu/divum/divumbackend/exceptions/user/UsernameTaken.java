package eu.divum.divumbackend.exceptions.user;

public class UsernameTaken extends RuntimeException {
    public UsernameTaken(String message) {
        super(message);
    }
}
