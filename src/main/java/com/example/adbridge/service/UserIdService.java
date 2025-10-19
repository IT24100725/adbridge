package com.example.adbridge.service;

import com.example.adbridge.repo.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserIdService {

    private final UserRepository userRepository;

    public UserIdService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUserId() {
        // Get the count of existing users to generate the next ID
        long userCount = userRepository.count();
        String userId = String.format("U%04d", userCount + 1);
        
        // Check if this ID already exists (safety check)
        while (userRepository.findByUserId(userId).isPresent()) {
            userCount++;
            userId = String.format("U%04d", userCount + 1);
        }
        
        return userId;
    }
}

