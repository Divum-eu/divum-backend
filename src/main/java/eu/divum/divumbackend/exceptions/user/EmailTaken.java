package eu.divum.divumbackend.exceptions.user;

public class EmailTaken extends RuntimeException {
    public EmailTaken(String message) {
        super(message);
    }
}
