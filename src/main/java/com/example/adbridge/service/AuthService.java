package com.example.adbridge.service;

import com.example.adbridge.model.User;
import com.example.adbridge.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserIdService userIdService;
    private final boolean debug = true; // set to false when done debugging

    public AuthService(UserRepository userRepository, UserIdService userIdService) {
        this.userRepository = userRepository;
        this.userIdService = userIdService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String register(String fullName, String email, String phone, String companyName, String username, String password, String confirmPassword) {
        fullName = safeTrim(fullName);
        email = safeTrim(email);
        phone = safeTrim(phone);
        companyName = safeTrim(companyName);
        username = safeTrim(username);
        password = safeTrim(password);
        confirmPassword = safeTrim(confirmPassword);

        // Validation
        if (blank(fullName) || blank(email) || blank(phone) || blank(username) || blank(password)) {
            return "Full name, email, phone, username, and password are required";
        }
        
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        
        // Enhanced password validation
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            return passwordError;
        }
        
        if (username.length() < 3 || username.length() > 30) {
            return "Username must be between 3 and 30 characters";
        }
        
        if (userRepository.existsByEmail(email)) {
            return "Email already exists";
        }
        
        if (userRepository.existsByPhone(phone)) {
            return "Phone number already exists";
        }
        
        if (userRepository.existsByUsername(username)) {
            return "Username already exists";
        }

        // Generate unique user ID
        String userId = userIdService.generateUserId();
        
        // Create new user
        User user = new User(userId, fullName, email, phone, companyName, username, passwordEncoder.encode(password));
        userRepository.save(user);
        
        if (debug) {
            System.out.println("[REGISTER] Saved user userId=" + userId + " email=" + email + " phone=" + phone);
        }
        return null; // null = success
    }

    public Optional<User> login(String username, String password) {
        username = safeTrim(username);
        password = safeTrim(password);
        
        if (blank(username) || blank(password)) return Optional.empty();

        if (debug) {
            System.out.println("[LOGIN] Attempt username='" + username + "'");
        }

        Optional<User> found = userRepository.findByUsername(username);
        if (found.isPresent()) {
            User user = found.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                if (debug) System.out.println("[LOGIN] PASSWORD MATCH");
                return Optional.of(user);
            } else {
                if (debug) System.out.println("[LOGIN] PASSWORD MISMATCH");
                return Optional.empty();
            }
        } else {
            if (debug) System.out.println("[LOGIN] No user with username='" + username + "'");
            return Optional.empty();
        }
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
    
    private String validatePassword(String password) {
        // Check minimum length (8 characters)
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        // Check for uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        
        // Check for special symbols
        if (!password.matches(".*[@#$%^&+=!].*")) {
            return "Password must contain at least one special symbol (@, #, $, %, ^, &, +, =, !)";
        }
        
        return null; // Password is valid
    }
}