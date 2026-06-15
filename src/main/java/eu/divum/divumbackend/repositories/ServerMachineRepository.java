package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.ServerMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServerMachineRepository extends JpaRepository<ServerMachine, UUID> {
    @Query("SELECT s FROM ServerMachine s " +
            "WHERE s.freeCpuCores > :reqCpuCores" +
            "  AND s.freeRamMb > :reqRamMb")
    List<ServerMachine> findAllAvailable(@Param("reqCpuCores") float cpuCores, @Param("reqRamMb") int ramMb);
}
