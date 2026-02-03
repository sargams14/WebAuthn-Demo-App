package com.example.WebAuthn_Demo_App.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationFinishRequest {
    private String username;
    private String id;
    private String rawId;
    private String type;
    private AssertionResponse response;

    @Data
    @NoArgsConstructor
    public static class AssertionResponse {
        private String authenticatorData;
        private String clientDataJSON;
        private String signature;
        private String userHandle;
    }
}
