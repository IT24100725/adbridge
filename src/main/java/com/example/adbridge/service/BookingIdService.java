package com.example.adbridge.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class BookingIdService {
    
    public String generateUniqueBookingId() {
        // Format: ADB-YYYYMMDD-HHMMSS-XXXX
        // ADB = AdBridge prefix
        // YYYYMMDD = Date
        // HHMMSS = Time
        // XXXX = Random 4-digit number
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        
        String date = now.format(dateFormatter);
        String time = now.format(timeFormatter);
        
        // Generate random 4-digit number
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000; // 1000-9999
        
        return String.format("ADB-%s-%s-%04d", date, time, randomNumber);
    }
}

