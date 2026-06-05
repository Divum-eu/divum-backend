package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        eu.divum.divumbackend.domain.User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFound("User with username " + username + " not found")
        );
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordData(),
                Collections.emptyList()
        );
    }
}
