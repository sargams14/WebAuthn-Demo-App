package com.example.WebAuthn_Demo_App.model;

import com.webauthn4j.credential.CredentialRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credential {
    private CredentialRecord record;

    public byte[] getCredentialId() {
        return record.getAttestedCredentialData().getCredentialId();
    }
}
