package eu.divum.divumbackend.dtos.cloudflare;

public record CloudflareDNSRequest(String name, int ttl, String type, String content) {
}
