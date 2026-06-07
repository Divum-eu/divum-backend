package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;

public interface UserService {
    GetUserResponse get(String id);

    String create(CreateUserRequest request);

    void update(String id, UpdateUserRequest request);

    void delete(String id);
}
