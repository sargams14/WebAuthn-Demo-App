package com.example.WebAuthn_Demo_App.store;

import com.example.WebAuthn_Demo_App.model.Credential;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CredentialStore {
    private final Map<String, Credential> credentials = new ConcurrentHashMap<>();

    public void save(String username, Credential credential) {
        credentials.put(username, credential);
    }

    public Credential get(String username) {
        return credentials.get(username);
    }

    public boolean exists(String username) {
        return credentials.containsKey(username);
    }
}
