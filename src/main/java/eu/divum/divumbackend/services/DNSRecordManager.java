package eu.divum.divumbackend.services;

import java.io.IOException;

public interface DNSRecordManager {
    String create(String domain, String ipAddress);
    boolean delete(String domain);
}
