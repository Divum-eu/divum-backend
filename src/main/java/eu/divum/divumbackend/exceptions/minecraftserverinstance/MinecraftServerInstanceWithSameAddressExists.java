package eu.divum.divumbackend.exceptions.minecraftserverinstance;

public class MinecraftServerInstanceWithSameAddressExists extends RuntimeException {
    public MinecraftServerInstanceWithSameAddressExists(String message) {
        super(message);
    }
}
