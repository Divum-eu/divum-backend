package eu.divum.divumbackend.services;

public interface DNSRecordManager {
    String create(String domain, String ipAddress);
    boolean delete(String domain);
}
