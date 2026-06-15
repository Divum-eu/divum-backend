package eu.divum.divumbackend.mappers.user;

import eu.divum.divumbackend.domain.User;

import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.dtos.user.GetUserResponse;

import eu.divum.divumbackend.dtos.user.UpdateUserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    GetUserResponse mapToGetDto(User user);

    User mapToEntity(CreateUserRequest request);

    UpdateUserResponse mapToUpdateDto(User user);
}
