package eu.divum.divumbackend.dtos.cloudflare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudflareDNSRecord(
        String id,
        String name,
        String type,
        String content
) {
}
