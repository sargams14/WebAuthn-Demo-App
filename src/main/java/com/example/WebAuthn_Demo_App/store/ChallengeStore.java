package com.example.WebAuthn_Demo_App.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChallengeStore {
    private final Map<String, byte[]> registrationChallenges = new ConcurrentHashMap<>();
    private final Map<String, byte[]> authenticationChallenges = new ConcurrentHashMap<>();

    public void saveRegistration(String username, byte[] challenge) {
        registrationChallenges.put(username, challenge);
    }

    public byte[] takeRegistration(String username) {
        return registrationChallenges.remove(username);
    }

    public void saveAuthentication(String username, byte[] challenge) {
        authenticationChallenges.put(username, challenge);
    }

    public byte[] takeAuthentication(String username) {
        return authenticationChallenges.remove(username);
    }
}
