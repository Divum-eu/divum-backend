package eu.divum.divumbackend.dtos.cloudflare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudflareDNSListResponse(
        boolean success,
        List<CloudflareDNSRecord> result
) {
}
