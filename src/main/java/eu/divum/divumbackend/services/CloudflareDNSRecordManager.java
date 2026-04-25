package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSCreateResponse;
import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSListResponse;
import eu.divum.divumbackend.dtos.cloudflare.CloudflareDNSRequest;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

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

    private final JsonMapper jsonMapper = new JsonMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

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
                throw new RuntimeException("Cloudflare API error");
            }

            CloudflareDNSCreateResponse data = jsonMapper.readValue(response.body(), CloudflareDNSCreateResponse.class);

            return data.result().name();

        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException("HTTP request failed");
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
                    .header("Content-Type", "Application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .DELETE()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;

            } catch (IOException | InterruptedException exception) {
                throw new RuntimeException("HTTP request failed");
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
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        // Strict IPv4 regex: ensures four octets, each between 0 and 255, separated by dots.
        // It also prevents leading zeros (e.g., "01.02.03.04") which can cause octal conversion bugs.
        String ipv4Regex = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";

        return ipAddress.matches(ipv4Regex);
    }

    private String isAlreadyRegistered(String domain) {
        // Returns the DNS record ID if registered, else returns null

        String endpoint = String.format(API_URL, zoneId) + "?name.exact=" + domain;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "Application/json")
                .header("Authorization", "Bearer " + apiToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) {
                throw new RuntimeException("Cloudflare API error");
            }

            CloudflareDNSListResponse dnsListResponse = jsonMapper.readValue(response.body(), CloudflareDNSListResponse.class);

            return dnsListResponse.result().isEmpty() ? null : dnsListResponse.result().getFirst().id();

        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException("HTTP request failed");
        }
    }
}
