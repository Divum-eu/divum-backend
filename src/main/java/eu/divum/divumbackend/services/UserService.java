package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;
import eu.divum.divumbackend.dtos.user.UpdateUserResponse;

public interface UserService {
    GetUserResponse get(String id);

    String create(CreateUserRequest request);

    UpdateUserResponse update(String id, UpdateUserRequest request);

    void delete(String id);
}
