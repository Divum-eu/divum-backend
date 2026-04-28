package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSCreateResponse;
import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSListResponse;
import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSRequest;

import eu.divum.divumbackend.exceptions.HTTPRequestException;

import eu.divum.divumbackend.exceptions.cloudflare.CloudflareAPIException;

import eu.divum.divumbackend.services.implementations.DNSRecordManager;
import lombok.RequiredArgsConstructor;

import org.apache.commons.validator.routines.InetAddressValidator;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
@Service
public class CloudflareDNSRecordManager implements DNSRecordManager {
    // Follows the documentation at: https://developers.cloudflare.com/api/resources/dns

    private static final String API_URL = "https://api.cloudflare.com/client/v4/zones/%s/dns_records";

    @Value("${cloudflare.zone-id}")
    private String zoneId;

    @Value("${cloudflare.domain-api-token}")
    private String apiToken;

    @Value("${app.domain}")
    private String domain;

    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;

    @Override
    public String create(String domain, String ipAddress) {
        if (!isValidIpv4(ipAddress)) {
            throw new IllegalArgumentException("Invalid IPv4 address");
        }
        if (!isValidDomain(domain) || isAlreadyRegistered(domain) != null) {
            throw new IllegalArgumentException("Invalid domain or already registered");
        }
        String endpoint = String.format(API_URL, zoneId);

        String payload = jsonMapper.writeValueAsString(
                // ttl = 0 so it sets to Auto
                new CloudflareDNSRequest(domain, 0, "A", ipAddress)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) {
                throw new CloudflareAPIException("Cloudflare API exception");
            }

            CloudflareDNSCreateResponse data = jsonMapper.readValue(response.body(), CloudflareDNSCreateResponse.class);

            return data.result().name();

        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }

    @Override
    public boolean delete(String domain) {
        if (!isValidDomain(domain)) return false;

        // gets the DNS record ID to send it in the request url
        String DNSRecordID = isAlreadyRegistered(domain);
        if (DNSRecordID != null) {
            String endpoint = String.format(API_URL, zoneId) + "/" + DNSRecordID;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + apiToken)
                    .DELETE()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;

            } catch (IOException | InterruptedException exception) {
                throw new HTTPRequestException();
            }
        }

        return true;
    }

    private boolean isValidDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }

        String lowerDomain = domain.toLowerCase();

        if (!lowerDomain.endsWith("." + this.domain)) {
            return false;
        }

        // remove .divum.eu to check the subdomain
        String subdomain = lowerDomain.substring(0, lowerDomain.length() - this.domain.length() - 1);

        // free cloudflare plan doesn't allow nested subdomains :(
        if (subdomain.contains(".")) {
            return false;
        }

        // - Must be between 1 and 63 characters long
        // - Can only contain lowercase letters, numbers, and hyphens
        // - Cannot start or end with a hyphen
        String validDnsRegex = "^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$";

        return subdomain.matches(validDnsRegex);
    }

    private boolean isValidIpv4(String ipAddress) {
        return InetAddressValidator.getInstance().isValidInet4Address(ipAddress);
    }

    private String isAlreadyRegistered(String domain) {
        // Returns the DNS record ID if registered, else returns null

        String endpoint = String.format(API_URL, zoneId) + "?name.exact=" + domain;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) {
                throw new CloudflareAPIException("Cloudflare API exception");
            }

            CloudflareDNSListResponse dnsListResponse = jsonMapper.readValue(response.body(), CloudflareDNSListResponse.class);

            return dnsListResponse.result().isEmpty() ? null : dnsListResponse.result().getFirst().id();

        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }
}
