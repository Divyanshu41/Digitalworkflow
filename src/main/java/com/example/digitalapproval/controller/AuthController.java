package com.example.digitalapproval.controller;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.digitalapproval.dto.ApiResponseDto;
import com.example.digitalapproval.dto.AuthMeResponseDto;
import com.example.digitalapproval.dto.RegisterRequestDto;
import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.RoleRepository;
import com.example.digitalapproval.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public AuthMeResponseDto me(Authentication authentication) {
        String email = authentication.getName();
        UserRepository.UserProfileView user = userRepository.findProfileByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return new AuthMeResponseDto(user.getId(), user.getEmail(), user.getRoleName().toUpperCase());
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        String normalizedEmail = registerRequest.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto("User already exists with this email"));
        }

        Role userRole = roleRepository.findByNameIgnoreCase("USER")
            .orElseGet(() -> {
                Role role = new Role();
                role.setName("USER");
                role.setDescription("Default user role");
                return roleRepository.save(role);
            });

        String firstName = registerRequest.firstName().trim();
        String lastName = registerRequest.lastName() == null ? null : registerRequest.lastName().trim();
        String passwordHash = passwordEncoder.encode(registerRequest.password());

        User user = new User();
        user.setRole(userRole);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponseDto("Registration successful. Please login."));
    }
}