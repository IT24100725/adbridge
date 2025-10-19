package com.example.adbridge.config;

import com.example.adbridge.controller.AdminAuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminSessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();

        // Allow login + logout pages through
        if (path.equals("/admin/login") || path.equals("/admin/logout")) {
            return true;
        }

        // Only guard /admin/** paths
        if (path.startsWith("/admin/")) {
            HttpSession session = request.getSession(false);
            boolean ok = session != null &&
                    Boolean.TRUE.equals(session.getAttribute(AdminAuthController.SESSION_KEY));
            if (!ok) {
                response.sendRedirect("/admin/login");
                return false;
            }
        }
        return true;
    }
}