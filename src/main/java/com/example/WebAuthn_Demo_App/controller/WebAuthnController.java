package com.example.WebAuthn_Demo_App.controller;

import com.example.WebAuthn_Demo_App.service.WebAuthnService;
import com.example.WebAuthn_Demo_App.web.dto.AuthenticationFinishRequest;
import com.example.WebAuthn_Demo_App.web.dto.RegistrationFinishRequest;
import com.example.WebAuthn_Demo_App.web.dto.UsernameRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webauthn")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    public WebAuthnController(WebAuthnService webAuthnService) {
        this.webAuthnService = webAuthnService;
    }

    @PostMapping("/register/options")
    public Map<String, Object> registrationOptions(@RequestBody UsernameRequest request) {
        return webAuthnService.startRegistration(request.getUsername());
    }

    @PostMapping("/register/verify")
    public Map<String, Object> finishRegistration(@RequestBody RegistrationFinishRequest request) {
        webAuthnService.finishRegistration(request);
        return Map.of("status", "ok");
    }

    @PostMapping("/authenticate/options")
    public Map<String, Object> authenticationOptions(@RequestBody UsernameRequest request) {
        return webAuthnService.startAuthentication(request.getUsername());
    }

    @PostMapping("/authenticate/verify")
    public Map<String, Object> finishAuthentication(@RequestBody AuthenticationFinishRequest request) {
        webAuthnService.finishAuthentication(request);
        return Map.of("status", "ok");
    }
}
