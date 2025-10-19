package com.example.adbridge.controller;

import com.example.adbridge.model.User;
import com.example.adbridge.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "next", required = false) String next,
                            Model model, HttpSession session) {
        // Check if user is already logged in
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            }
            return "redirect:/";
        }
        
        model.addAttribute("title", "Welcome back?");
        model.addAttribute("subtitle", "The faster you fill up, the faster you get a ticket");
        model.addAttribute("next", next);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       @RequestParam(value = "next", required = false) String next,
                       HttpSession session, 
                       Model model) {
        
        var user = authService.login(username, password);
        if (user.isPresent()) {
            session.setAttribute("user", user.get());
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            }
            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("title", "Welcome back?");
            model.addAttribute("subtitle", "The faster you fill up, the faster you get a ticket");
            model.addAttribute("next", next);
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(value = "next", required = false) String next,
                               Model model, HttpSession session) {
        // Check if user is already logged in
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            return "redirect:/";
        }
        
        model.addAttribute("title", "Create Account");
        model.addAttribute("subtitle", "Join us to get started with your advertising campaigns");
        model.addAttribute("user", new User());
        model.addAttribute("next", next);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String phone,
                          @RequestParam(required = false) String companyName,
                          @RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam(value = "next", required = false) String next,
                          Model model) {
        
        String error = authService.register(fullName, email, phone, companyName, username, password, confirmPassword);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("title", "Create Account");
            model.addAttribute("subtitle", "Join us to get started with your advertising campaigns");
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("companyName", companyName);
            model.addAttribute("username", username);
            model.addAttribute("next", next);
            return "register";
        } else {
            // Get the newly created user to show their ID
            var newUser = authService.findByUsername(username);
            if (newUser.isPresent()) {
                model.addAttribute("success", "Account created successfully! Your User ID is: " + newUser.get().getUserId() + ". Please log in.");
            } else {
                model.addAttribute("success", "Account created successfully! Please log in.");
            }
            if (next != null && !next.isBlank()) {
                return "redirect:/login?next=" + next;
            }
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }
}