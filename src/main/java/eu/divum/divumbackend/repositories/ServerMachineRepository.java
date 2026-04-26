package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.ServerMachine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServerMachineRepository extends JpaRepository<ServerMachine, UUID> {
}
