package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.ServerMachine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServerMachineRepository extends JpaRepository<ServerMachine, UUID> {
    @Query("SELECT s FROM ServerMachine s " +
            "WHERE s.freeCpuCores > :reqCores" +
            "  AND s.freeRam > :reqRam")
    List<ServerMachine> findAllAvailable(@Param("reqCores") int cores, @Param("reqRam") int ram);
}
