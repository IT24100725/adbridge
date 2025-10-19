package com.example.adbridge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.info("[NOTIFY] Skipped email: missing recipient. subject={} ", subject);
            return;
        }
        // Placeholder: integrate real SMTP/provider later
        log.info("[NOTIFY] Email queued >> to={} | subject={} | body={}", to, subject, truncate(body, 300));
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}








