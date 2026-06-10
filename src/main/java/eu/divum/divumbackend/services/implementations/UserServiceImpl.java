package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;

import eu.divum.divumbackend.dtos.user.CreateUserRequest;
import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;

import eu.divum.divumbackend.dtos.user.UpdateUserResponse;
import eu.divum.divumbackend.exceptions.user.EmailTaken;
import eu.divum.divumbackend.exceptions.user.SameUsernameUpdate;
import eu.divum.divumbackend.exceptions.user.UsernameTaken;
import eu.divum.divumbackend.mappers.user.UserMapper;

import eu.divum.divumbackend.exceptions.user.UserNotFound;

import eu.divum.divumbackend.repositories.UserRepository;

import eu.divum.divumbackend.services.AuthenticationService;
import eu.divum.divumbackend.services.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper mapper;

    private final AuthenticationService authService;

    @Override
    public GetUserResponse get(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new UserNotFound("No user exists with the given id."));

        return mapper.mapToGetDto(user);
    }

    @Override
    public String create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTaken("A user with the given username already exists.");
        }

        if (userRepository.existsByEmailAddress(request.emailAddress())) {
            throw new EmailTaken("A user with the given email already exists.");
        }

        User user = mapper.mapToEntity(request);

        user.setPasswordData(authService.generatePasswordHash(request.password()));

        userRepository.save(user);

        return user.getId().toString();
    }

    @Override
    public UpdateUserResponse update(String id, UpdateUserRequest request) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new UserNotFound("No user exists with the given id."));

        if (user.getUsername().equals(request.username())) {
            throw new SameUsernameUpdate("Cannot set username to the same value.");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTaken("A user with the given username already exists.");
        }

        user.setUsername(request.username());

        userRepository.save(user);

        return mapper.mapToUpdateDto(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() ->
                        new UserNotFound("No user exists with the given id."));

        userRepository.delete(user);
    }
}
