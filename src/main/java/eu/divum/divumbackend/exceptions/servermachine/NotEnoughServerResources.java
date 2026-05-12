package eu.divum.divumbackend.exceptions.servermachine;

public class NotEnoughServerResources extends RuntimeException {
    public NotEnoughServerResources(String s) {
        super(s);
    }
}
