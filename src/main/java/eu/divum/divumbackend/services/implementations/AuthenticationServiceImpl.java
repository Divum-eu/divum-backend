package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.services.AuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public String generatePasswordHash(String password) {
        return "";
    }
}
