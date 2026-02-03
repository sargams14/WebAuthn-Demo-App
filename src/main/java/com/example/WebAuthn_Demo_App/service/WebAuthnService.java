package com.example.WebAuthn_Demo_App.service;

import com.example.WebAuthn_Demo_App.model.Credential;
import com.example.WebAuthn_Demo_App.model.User;
import com.example.WebAuthn_Demo_App.web.dto.AuthenticationFinishRequest;
import com.example.WebAuthn_Demo_App.web.dto.RegistrationFinishRequest;
import com.example.WebAuthn_Demo_App.store.ChallengeStore;
import com.example.WebAuthn_Demo_App.store.CredentialStore;
import com.example.WebAuthn_Demo_App.store.UserStore;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class WebAuthnService {
    private static final List<PublicKeyCredentialParameters> PUB_KEY_CRED_PARAMS = List.of(
            new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256)
    );

    private final WebAuthnManager webAuthnManager;
    private final UserStore userStore;
    private final CredentialStore credentialStore;
    private final ChallengeStore challengeStore;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String rpId;
    private final String rpOrigin;
    private final String rpName;

    public WebAuthnService(WebAuthnManager webAuthnManager,
                           UserStore userStore,
                           CredentialStore credentialStore,
                           ChallengeStore challengeStore,
                           @Qualifier("relyingPartyId") String relyingPartyId,
                           @Qualifier("relyingPartyOrigin") String relyingPartyOrigin,
                           @Qualifier("relyingPartyName") String relyingPartyName) {
        this.webAuthnManager = webAuthnManager;
        this.userStore = userStore;
        this.credentialStore = credentialStore;
        this.challengeStore = challengeStore;
        this.rpId = relyingPartyId;
        this.rpOrigin = relyingPartyOrigin;
        this.rpName = relyingPartyName;
    }

    public Map<String, Object> startRegistration(String username) {
        String normalized = normalizeUsername(username);
        User user = userStore.getOrCreate(normalized);

        byte[] challenge = newChallenge();
        challengeStore.saveRegistration(normalized, challenge);

        Map<String, Object> options = new HashMap<>();
        options.put("challenge", Base64UrlUtil.encodeToString(challenge));
        options.put("rp", Map.of("name", rpName, "id", rpId));
        options.put("user", Map.of(
                "id", Base64UrlUtil.encodeToString(user.getUserId()),
                "name", user.getUsername(),
                "displayName", user.getUsername()
        ));
        options.put("pubKeyCredParams", List.of(
                Map.of("type", "public-key", "alg", COSEAlgorithmIdentifier.ES256.getValue())
        ));
        options.put("timeout", 60000);
        options.put("attestation", "none");

        Credential existing = credentialStore.get(normalized);
        if (existing != null) {
            options.put("excludeCredentials", List.of(
                    Map.of("type", "public-key", "id", Base64UrlUtil.encodeToString(existing.getCredentialId()))
            ));
        }

        return options;
    }

    public void finishRegistration(RegistrationFinishRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration response.");
        }
        String username = normalizeUsername(request.getUsername());
        byte[] challenge = challengeStore.takeRegistration(username);
        if (challenge == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing registration challenge.");
        }

        RegistrationFinishRequest.AttestationResponse response = requireRegistrationResponse(request);

        RegistrationRequest registrationRequest = new RegistrationRequest(
                Base64UrlUtil.decode(response.getAttestationObject()),
                Base64UrlUtil.decode(response.getClientDataJSON())
        );

        ServerProperty serverProperty = new ServerProperty(
                Origin.create(rpOrigin),
                rpId,
                new DefaultChallenge(challenge)
        );

        RegistrationParameters parameters = new RegistrationParameters(
                serverProperty,
                PUB_KEY_CRED_PARAMS,
                false
        );

        RegistrationData registrationData = webAuthnManager.validate(registrationRequest, parameters);
        byte[] credentialId = registrationData.getAttestationObject()
                .getAuthenticatorData()
                .getAttestedCredentialData()
                .getCredentialId();

        credentialStore.save(username, new Credential(
                credentialId,
                AuthenticatorImpl.createFromRegistrationData(registrationData)
        ));
    }

    public Map<String, Object> startAuthentication(String username) {
        String normalized = normalizeUsername(username);
        User user = userStore.get(normalized);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown user.");
        }

        Credential credential = credentialStore.get(normalized);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No credential registered.");
        }

        byte[] challenge = newChallenge();
        challengeStore.saveAuthentication(normalized, challenge);

        Map<String, Object> options = new HashMap<>();
        options.put("challenge", Base64UrlUtil.encodeToString(challenge));
        options.put("timeout", 60000);
        options.put("rpId", rpId);
        options.put("userVerification", "preferred");
        options.put("allowCredentials", List.of(
                Map.of("type", "public-key", "id", Base64UrlUtil.encodeToString(credential.getCredentialId()))
        ));

        return options;
    }

    public void finishAuthentication(AuthenticationFinishRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication response.");
        }
        String username = normalizeUsername(request.getUsername());
        byte[] challenge = challengeStore.takeAuthentication(username);
        if (challenge == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing authentication challenge.");
        }

        Credential credential = credentialStore.get(username);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No credential registered.");
        }

        AuthenticationFinishRequest.AssertionResponse response = requireAuthenticationResponse(request);
        byte[] requestCredentialId = Base64UrlUtil.decode(request.getRawId());
        if (!Arrays.equals(credential.getCredentialId(), requestCredentialId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credential mismatch.");
        }

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                requestCredentialId,
                decodeIfPresent(response.getUserHandle()),
                Base64UrlUtil.decode(response.getAuthenticatorData()),
                Base64UrlUtil.decode(response.getClientDataJSON()),
                null,
                Base64UrlUtil.decode(response.getSignature())
        );

        ServerProperty serverProperty = new ServerProperty(
                Origin.create(rpOrigin),
                rpId,
                new DefaultChallenge(challenge)
        );

        AuthenticationParameters parameters = new AuthenticationParameters(
                serverProperty,
                credential.getAuthenticator(),
                List.of(credential.getCredentialId()),
                false
        );

        AuthenticationData authenticationData = webAuthnManager.validate(authenticationRequest, parameters);
        credential.getAuthenticator().setCounter(authenticationData.getAuthenticatorData().getSignCount());
    }

    private byte[] newChallenge() {
        byte[] challenge = new byte[32];
        secureRandom.nextBytes(challenge);
        return challenge;
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        return username.trim().toLowerCase();
    }

    private RegistrationFinishRequest.AttestationResponse requireRegistrationResponse(RegistrationFinishRequest request) {
        if (request.getResponse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration response.");
        }
        return request.getResponse();
    }

    private AuthenticationFinishRequest.AssertionResponse requireAuthenticationResponse(AuthenticationFinishRequest request) {
        if (request.getResponse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication response.");
        }
        return request.getResponse();
    }

    private byte[] decodeIfPresent(String base64Url) {
        if (base64Url == null || base64Url.isBlank()) {
            return null;
        }
        return Base64UrlUtil.decode(base64Url);
    }
}
