package eu.divum.divumbackend.dtos.cloudflare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudflareDNSCreateResponse(
    boolean success,
    CloudflareDNSRecord result
) {}
