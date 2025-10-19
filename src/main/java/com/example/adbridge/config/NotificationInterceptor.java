package com.example.adbridge.config;

import com.example.adbridge.model.User;
import com.example.adbridge.repo.NotificationRepository;
import com.example.adbridge.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NotificationInterceptor implements HandlerInterceptor {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
                request.setAttribute("unreadNotificationCount", unreadCount);
            }
        }
        return true;
    }
}
