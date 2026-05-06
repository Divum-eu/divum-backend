package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.MinecraftServerInstance;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface MinecraftServerInstanceRepository extends JpaRepository<MinecraftServerInstance, UUID> {
    Optional<MinecraftServerInstance> findByAddress(String address);

    boolean existsByAddress(String address);
}
