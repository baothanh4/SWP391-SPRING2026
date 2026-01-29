package com.example.SWP391_SPRING2026.Controller;


import com.example.SWP391_SPRING2026.DTO.LoginRequest;
import com.example.SWP391_SPRING2026.DTO.LoginResponse;
import com.example.SWP391_SPRING2026.DTO.RegisterRequest;
import com.example.SWP391_SPRING2026.DTO.RegisterResponse;
import com.example.SWP391_SPRING2026.Service.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
