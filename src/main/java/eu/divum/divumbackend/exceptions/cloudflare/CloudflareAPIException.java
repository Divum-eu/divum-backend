package eu.divum.divumbackend.exceptions.cloudflare;

public class CloudflareAPIException extends RuntimeException {
    public CloudflareAPIException(String message) {
        super(message);
    }
}
