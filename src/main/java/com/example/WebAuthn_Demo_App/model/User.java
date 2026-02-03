package com.example.WebAuthn_Demo_App.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private String username;
    private byte[] userId;
}
