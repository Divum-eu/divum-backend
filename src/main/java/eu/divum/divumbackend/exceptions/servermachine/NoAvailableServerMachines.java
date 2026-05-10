package eu.divum.divumbackend.exceptions.servermachine;

public class NoAvailableServerMachines extends RuntimeException {
    public NoAvailableServerMachines(String message) {
        super(message);
    }
}
