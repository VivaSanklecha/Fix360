package com.example.fix360.Controller;

import com.example.fix360.Entity.Users;
import com.example.fix360.repo.UsersRepo;
import com.example.fix360.dto.AuthResponseDTO;
import com.example.fix360.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Users user) {
        // Check if email already exists
        if (usersRepo.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(400).body("Email is already registered!");
        }
        // Only allow USER, ADMIN, or SERVICE_PROVIDER roles
        String role = user.getRole();
        if (role == null || (!role.equalsIgnoreCase("USER") && !role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("SERVICE_PROVIDER"))) {
            user.setRole("USER");
        }
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepo.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Users loginRequest) {
    System.out.println("Login endpoint hit with: " + loginRequest.getEmail());
    try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        String token = jwtUtil.generateToken(loginRequest.getEmail());
        System.out.println("Token generated: " + token);
        return ResponseEntity.ok(new AuthResponseDTO(token));
    } catch (AuthenticationException e) {
        System.out.println("Authentication failed for: " + loginRequest.getEmail());
        return ResponseEntity.status(401).body("Invalid email or password!");
    }
}
    }
    