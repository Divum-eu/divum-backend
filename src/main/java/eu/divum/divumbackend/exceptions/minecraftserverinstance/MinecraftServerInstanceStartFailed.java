package eu.divum.divumbackend.exceptions.minecraftserverinstance;

public class MinecraftServerInstanceStartFailed extends RuntimeException {
    public MinecraftServerInstanceStartFailed(String message) {
        super(message);
    }
}
