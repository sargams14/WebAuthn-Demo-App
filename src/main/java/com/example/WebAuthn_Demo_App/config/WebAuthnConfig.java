package com.example.WebAuthn_Demo_App.config;

import com.webauthn4j.WebAuthnManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthnConfig {

    @Bean
    public WebAuthnManager webAuthnManager() {
        return WebAuthnManager.createNonStrictWebAuthnManager();
    }

    @Bean
    public String relyingPartyId(@Value("${webauthn.rp.id:localhost}") String rpId) {
        return rpId;
    }

    @Bean
    public String relyingPartyOrigin(@Value("${webauthn.rp.origin:http://localhost:8080}") String rpOrigin) {
        return rpOrigin;
    }

    @Bean
    public String relyingPartyName(@Value("${webauthn.rp.name:WebAuthn Demo}") String rpName) {
        return rpName;
    }
}
