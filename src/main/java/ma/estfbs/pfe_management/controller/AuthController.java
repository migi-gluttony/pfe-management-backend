package ma.estfbs.pfe_management.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.AuthResponse;
import ma.estfbs.pfe_management.dto.LoginRequest;
import ma.estfbs.pfe_management.dto.PasswordResetConfirm;
import ma.estfbs.pfe_management.dto.PasswordResetRequest;
import ma.estfbs.pfe_management.dto.RegisterRequest;
import ma.estfbs.pfe_management.service.AuthenticationService;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(service.register(registerRequest));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(service.login(loginRequest));
    }
    
    /**
     * Step 1 of password reset - validate user info and issue token
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<AuthResponse> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(service.requestPasswordReset(request));
    }

    /**
     * Step 2 of password reset - validate token and update password
     */
    @PostMapping("/reset-password-confirm")
    public ResponseEntity<AuthResponse> confirmPasswordReset(@RequestBody PasswordResetConfirm request) {
        return ResponseEntity.ok(service.confirmPasswordReset(request));
    }
}