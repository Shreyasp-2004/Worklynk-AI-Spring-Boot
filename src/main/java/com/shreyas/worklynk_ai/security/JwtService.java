package com.shreyas.worklynk_ai.security;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    private final byte[] secret;
    private final long expiryHours;

    public JwtService(
            @Value("${worklynk.jwt.secret:}") String secret,
            @Value("${worklynk.jwt.expiry-hours:8}") long expiryHours) {

        byte[] bytes = secret == null
                ? new byte[0]
                : secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "worklynk.jwt.secret must be set and at least 32 "
                    + "characters long (see application-local.yaml).");
        }

        this.secret = bytes;
        this.expiryHours = expiryHours;
    }

    public String generateToken(AuthUser user) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.employeeId()))
                    .claim("email", user.email())
                    .claim("role", user.role())
                    .claim("companyId", user.companyId())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(
                            now.plus(expiryHours, ChronoUnit.HOURS)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign token", e);
        }
    }

    /** Returns the user if the token is valid, correctly signed and unexpired. */
    public Optional<AuthUser> parse(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                return Optional.empty();
            }
            if (!jwt.verify(new MACVerifier(secret))) {
                return Optional.empty();
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiry = claims.getExpirationTime();
            if (expiry == null || expiry.before(new Date())) {
                return Optional.empty();
            }

            return Optional.of(new AuthUser(
                    Long.parseLong(claims.getSubject()),
                    claims.getStringClaim("email"),
                    claims.getStringClaim("role"),
                    claims.getLongClaim("companyId")));
        } catch (ParseException | JOSEException | RuntimeException e) {
            return Optional.empty();
        }
    }
}
