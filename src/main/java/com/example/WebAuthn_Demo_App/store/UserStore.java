package com.example.WebAuthn_Demo_App.store;

import com.example.WebAuthn_Demo_App.model.User;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStore {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public User getOrCreate(String username) {
        return users.computeIfAbsent(username, this::createUser);
    }

    public User get(String username) {
        return users.get(username);
    }

    private User createUser(String username) {
        byte[] userId = new byte[16];
        secureRandom.nextBytes(userId);
        return new User(username, userId);
    }
}
