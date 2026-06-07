package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;

import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;

import eu.divum.divumbackend.mappers.user.UserMapper;

import eu.divum.divumbackend.exceptions.user.UserNotFound;

import eu.divum.divumbackend.repositories.UserRepository;

import eu.divum.divumbackend.services.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final UserMapper dtoMapper;

    @Override
    public GetUserResponse get(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new UserNotFound("No user exists with the given id."));

        return dtoMapper.mapToResponse(user);
    }

    @Override
    public String create(CreateUserRequest request) {
        return "";
    }

    @Override
    public void update(String id, UpdateUserRequest request) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new UserNotFound("No user exists with the given id."));

        user.setUsername(request.username());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new  UserNotFound("No user exists with the given id."));

        userRepository.delete(user);
    }
}
