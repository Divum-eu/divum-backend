package eu.divum.divumbackend.repositories;

import eu.divum.divumbackend.domain.MinecraftServerInstance;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface MinecraftServerInstanceRepository extends JpaRepository<MinecraftServerInstance, UUID> {
    Optional<MinecraftServerInstance> findByAddress(String address);

    boolean existsByAddress(String address);
}
