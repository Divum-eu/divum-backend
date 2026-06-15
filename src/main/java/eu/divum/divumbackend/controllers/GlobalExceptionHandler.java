package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.exceptions.HTTPRequestException;
import eu.divum.divumbackend.exceptions.jwt.JwtSigningContextGenerationError;
import eu.divum.divumbackend.exceptions.jwt.JwtWriteError;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.*;
import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;
import eu.divum.divumbackend.exceptions.servermachine.NotEnoughServerResources;
import eu.divum.divumbackend.exceptions.user.EmailTaken;
import eu.divum.divumbackend.exceptions.user.SameUsernameUpdate;
import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.user.UsernameTaken;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserNotFound.class)
    public ProblemDetail handleUserNotFoundException(UserNotFound ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("User not found.");
        return problemDetail;
    }

    @ExceptionHandler(UsernameTaken.class)
    public ProblemDetail handleUsernameTaken(UsernameTaken ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setTitle("Username is already taken.");

        return problemDetail;
    }

    @ExceptionHandler(EmailTaken.class)
    public ProblemDetail handleEmailTaken(EmailTaken ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setTitle("Email is already in use.");

        return problemDetail;
    }

    @ExceptionHandler(SameUsernameUpdate.class)
    public ProblemDetail handleSameUsernameUpdate(SameUsernameUpdate ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage()
        );

        problemDetail.setTitle("Username update with the same username.");

        return problemDetail;
    }

    @ExceptionHandler(HTTPRequestException.class)
    public ProblemDetail handleHTTPRequestException() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "A call to an external API failed."
        );
    }

    @ExceptionHandler(MinecraftServerInstanceNotFound.class)
    public ProblemDetail handleMinecraftServerInstanceNotFound(MinecraftServerInstanceNotFound ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Minecraft server instance not found.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceStartFailed.class)
    public ProblemDetail handleMinecraftInstanceStartFailed(MinecraftServerInstanceStartFailed ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Couldn't start Minecraft instance.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceStopFailed.class)
    public ProblemDetail handleMinecraftServerInstanceStopFailed(MinecraftServerInstanceStopFailed ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Couldn't stop Minecraft instance.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceDeleteFailed.class)
    public ProblemDetail handleMinecraftServerInstanceDeleteFailed(MinecraftServerInstanceDeleteFailed ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Couldn't delete Minecraft instance.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceWithSameAddressExists.class)
    public ProblemDetail handleMinecraftServerInstanceWithSameAddressExists(MinecraftServerInstanceWithSameAddressExists ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Minecraft server instance with the same address exists.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceCreationFailed.class)
    public ProblemDetail handleMinecraftServerInstanceCreationFailed(MinecraftServerInstanceCreationFailed ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Couldn't create Minecraft server instance.");
        return problemDetail;
    }

    @ExceptionHandler(MinecraftServerInstanceUpdateFailed.class)
    public ProblemDetail handleMinecraftServerInstanceUpdateFailed(MinecraftServerInstanceUpdateFailed ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Couldn't update Minecraft server instance.");
        return problemDetail;
    }

    @ExceptionHandler(NoAvailableServerMachines.class)
    public ProblemDetail handleNoAvailableServerMachines(NoAvailableServerMachines ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("No server machines for the given RAM and CPU requirements are available.");
        return problemDetail;
    }

    @ExceptionHandler(NotEnoughServerResources.class)
    public ProblemDetail handleNotEnoughServerResources(NotEnoughServerResources ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Not enough server resources for the given RAM and CPU requirements are available.");
        return problemDetail;
    }

    @ExceptionHandler(JwtWriteError.class)
    public ProblemDetail handleJwtWriteError(JwtWriteError ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problemDetail.setTitle("An issue occurred trying to sign a JWT.");
        return problemDetail;
    }

    @ExceptionHandler(JwtSigningContextGenerationError.class)
    public ProblemDetail handleJwtSigningContextGenerationError(JwtSigningContextGenerationError ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problemDetail.setTitle("An issue occurred trying to generate a JWT signing key.");
        return problemDetail;
    }


    @ExceptionHandler(MinecraftServerInstanceNotOwned.class)
    public ProblemDetail handleMinecraftServerInstanceNotOwned() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Minecraft server instance not found."
        );

        problemDetail.setTitle("Not found");
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Wrong credentials."
        );

        problemDetail.setTitle("Wrong credentials");
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials."
        );

        problemDetail.setTitle("Authentication failed");
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The provided data conflicts with an existing record. Please check your inputs and try again."
        );

        problemDetail.setTitle("Data conflict");
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid request body."
        );
        problemDetail.setTitle("Validation Error");

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        problemDetail.setProperty("invalid_fields", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                String fieldPath = invalidFormatException.getPath().stream()
                        .map(ref -> {
                            if (ref.getPropertyName() != null) {
                                return ref.getPropertyName();
                            }
                            else if (ref.getIndex() >= 0) {
                                return "[" + ref.getIndex() + "]";
                            }
                            return "";
                        })
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.joining("."));

                fieldPath = fieldPath.replace(".[", "[");

                Object[] enumConstants = invalidFormatException.getTargetType().getEnumConstants();
                String allowedValues = Arrays.stream(enumConstants)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                String message = String.format("'%s' is not recognised. Allowed values: [%s]",
                        invalidFormatException.getValue(), allowedValues);

                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Invalid request body."
                );
                problemDetail.setTitle("Validation Error");

                Map<String, String> fieldErrors = new HashMap<>();
                fieldErrors.put(fieldPath, message);

                problemDetail.setProperty("invalid_fields", fieldErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
            }
        }

        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }
}
