package com.example.WebAuthn_Demo_App.model;

import com.webauthn4j.authenticator.Authenticator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credential {
    private byte[] credentialId;
    private Authenticator authenticator;
}
