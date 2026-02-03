package com.example.WebAuthn_Demo_App.config;

import com.webauthn4j.WebAuthnManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthnConfig {

    @Bean
    public WebAuthnManager webAuthnManager() {
        // Non-strict manager simplifies attestation for PoC.
        return WebAuthnManager.createNonStrictWebAuthnManager();
    }

    @Bean
    public String relyingPartyId(@Value("${webauthn.rp.id:localhost}") String rpId) {
        // rpId must match what the browser sends (domain). For localhost PoC use "localhost"
        return rpId;
    }

    @Bean
    public String relyingPartyOrigin(@Value("${webauthn.rp.origin:http://localhost:8080}") String rpOrigin) {
        // the exact origin your frontend will run on (include scheme + port)
        return rpOrigin;
    }

    @Bean
    public String relyingPartyName(@Value("${webauthn.rp.name:WebAuthn Demo}") String rpName) {
        return rpName;
    }
}
