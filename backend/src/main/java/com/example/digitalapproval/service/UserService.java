package com.example.digitalapproval.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.digitalapproval.dto.UserDTO;
import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    public User registerUser(String username, String email, String password, Role role) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new RuntimeException("Username or email already exists");
        }
        User user = new User(username, email, passwordEncoder.encode(password), role);
        User saved = userRepository.save(user);
        auditLogService.recordAction(saved.getId(), "USER_REGISTERED", "User registered with username " + username);
        return saved;
    }

    public User ensureUser(String username, String email, String password, Role role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isEmpty()) {
            return registerUser(username, email, password, role);
        }

        User user = existing.get();
        boolean updated = false;

        if (!email.equals(user.getEmail())) {
            user.setEmail(email);
            updated = true;
        }

        if (user.getRole() != role) {
            user.setRole(role);
            updated = true;
        }

        if (!passwordMatches(password, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            updated = true;
        }

        if (updated) {
            User saved = userRepository.save(user);
            auditLogService.recordAction(saved.getId(), "USER_BOOTSTRAP_UPDATED", "Bootstrap ensured user " + username);
            return saved;
        }

        return user;
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserDTO> getAllUserSummaries() {
        return userRepository.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    public UserDTO updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        User updated = userRepository.save(user);
        auditLogService.recordAction(userId, "USER_ROLE_UPDATED", "Role updated to " + role);
        return new UserDTO(updated);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        auditLogService.recordAction(userId, "USER_DELETED", "User account deleted");
    }
}