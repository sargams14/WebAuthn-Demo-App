package com.example.WebAuthn_Demo_App.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationFinishRequest {
    private String username;
    private String id;
    private String rawId;
    private String type;
    private AttestationResponse response;

    @Data
    @NoArgsConstructor
    public static class AttestationResponse {
        private String attestationObject;
        private String clientDataJSON;
    }
}
