package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.JwtSigningContext;
import eu.divum.divumbackend.exceptions.jwt.JwtSigningContextGenerationError;
import eu.divum.divumbackend.exceptions.jwt.JwtWriteError;
import eu.divum.divumbackend.services.JwtTokenService;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.JWSAlgorithm;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value("${divum-daemon.jwt-token-lifetime-seconds}")
    private int jwtTokenLifetimeSeconds;

    @Value("${security.jwt.key-rotation-period-hours}")
    private int keyRotationPeriodHours;

    private final ConcurrentHashMap<String, JwtSigningContext> signingKeysByKeyId =  new ConcurrentHashMap<String, JwtSigningContext>();

    private volatile String currentActiveKeyId;

    @Override
    public String writeSignedToken(Map<String, String> claims, String issuer) throws JwtWriteError
    {
        JwtSigningContext signingContext = signingKeysByKeyId.get(currentActiveKeyId);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .keyID(currentActiveKeyId)
                .build();

        Date currentTime = new Date();

        Date expirationTime = new Date(currentTime.getTime() + jwtTokenLifetimeSeconds * 1000L);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .issueTime(currentTime)
                .expirationTime(expirationTime);

        claims.forEach(claimsBuilder::claim);

        JWTClaimsSet payload = claimsBuilder.build();

        SignedJWT signedJWT = new SignedJWT(header, payload);

        try {
            signedJWT.sign(signingContext.getSigner());

            return signedJWT.serialize();
        } catch (com.nimbusds.jose.JOSEException e) {
            throw new JwtWriteError("Couldn't sign JWT");
        }
    }

    @Override
    public Map<String, Object> getPublicKeys() {
        List<JWK> keys = signingKeysByKeyId.values().stream()
                .map(c -> c.getKeyPair().toPublicJWK())
                .collect(Collectors.toList());

        return new JWKSet(keys).toJSONObject();
    }

    @PostConstruct
    private void initSigningContext() {
        rotateJwtSigningContext();
    }

    @Scheduled(
            fixedRateString = "${security.jwt.key-rotation-period-hours}",
            timeUnit = TimeUnit.HOURS)
    private void rotateJwtSigningContext() {
        String keyId = UUID.randomUUID().toString();

        JwtSigningContext signingContext = generateJwtSigningContext(keyId);

        signingKeysByKeyId.put(keyId, signingContext);

        currentActiveKeyId = keyId;

        signingKeysByKeyId.values().removeIf(c -> c.getExpirationTime() <= System.currentTimeMillis());
    }

    private JwtSigningContext generateJwtSigningContext(String keyId) throws JwtSigningContextGenerationError {
        Curve curve = Curve.Ed25519;

        try {
            OctetKeyPair keyPair = new OctetKeyPairGenerator(curve)
                    .keyID(keyId)
                    .generate();

            return new JwtSigningContext(
                    keyPair,
                    keyRotationPeriodHours * 3600L + jwtTokenLifetimeSeconds);
        } catch (com.nimbusds.jose.JOSEException e) {
            throw new JwtSigningContextGenerationError("Couldn't generate a " + curve.getName() + " key pair.");
        }
    }
}