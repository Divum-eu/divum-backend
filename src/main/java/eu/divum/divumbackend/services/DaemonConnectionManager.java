package eu.divum.divumbackend.services;

public interface DaemonConnectionManager {

    void connectIfNeeded(String daemonUrl, String instanceId);

    void disconnectIfEmpty(String daemonUrl);

    void removeDeadConnection(String daemonUrl);
}
