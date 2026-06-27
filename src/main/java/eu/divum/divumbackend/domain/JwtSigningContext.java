package eu.divum.divumbackend.domain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.crypto.Ed25519Signer;

import lombok.Getter;

@Getter
public class JwtSigningContext {
    private final OctetKeyPair keyPair;
    private final Ed25519Signer signer;
    private final long expirationTime;

    public JwtSigningContext(OctetKeyPair keyPair, long expirationTimeSeconds) throws JOSEException {
        this.keyPair = keyPair;
        this.signer = new Ed25519Signer(keyPair);
        this.expirationTime = System.currentTimeMillis() + expirationTimeSeconds * 1000;
    }
}
