package eu.divum.divumbackend.exceptions.user;

public class SameUsernameUpdate extends RuntimeException {
    public SameUsernameUpdate(String message) {
        super(message);
    }
}
