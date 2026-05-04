package eu.divum.divumbackend.mappers.minecraftserverinstance;

import eu.divum.divumbackend.domain.MinecraftServerInstance;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceConfiguration;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MinecraftServerInstanceMapper {
   MinecraftServerInstanceResponse mapToResponse(MinecraftServerInstance entity);

   MinecraftServerInstance mapToEntity(MinecraftServerInstanceRequest inputDto);
}
