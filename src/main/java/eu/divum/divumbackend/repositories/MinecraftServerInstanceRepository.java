package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.MinecraftServerInstance;
import eu.divum.divumbackend.dtos.minecraftserverinstance.DaemonConnectionInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MinecraftServerInstanceRepository extends JpaRepository<MinecraftServerInstance, UUID> {
    Optional<MinecraftServerInstance> findByAddress(String address);

    boolean existsByAddress(String address);

    @Query("SELECT new eu.divum.divumbackend.dtos.minecraftserverinstance.DaemonConnectionInfo(sm.ip, msi.daemonId) " +
            "FROM MinecraftServerInstance msi " +
            "JOIN msi.serverMachine sm " +
            "WHERE msi.id = :instanceId")
    Optional<DaemonConnectionInfo> findDaemonConnectionInfoById(@Param("instanceId") UUID instanceId);

    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT msi FROM MinecraftServerInstance msi WHERE msi.id = :id")
    Optional<MinecraftServerInstance> findByIdWithOwner(@Param("id") UUID id);
}
