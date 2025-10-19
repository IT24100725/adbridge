package com.example.adbridge.controller;

import com.example.adbridge.model.User;
import com.example.adbridge.repo.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String userManagement(@RequestParam(required = false) String search, Model model) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
        } else {
            users = userRepository.findAll();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("title", "User Management · Admin");
        model.addAttribute("pageHeading", "User Management");
        model.addAttribute("activeMenu", "users");
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Add User · Admin");
        model.addAttribute("pageHeading", "Create New User");
        model.addAttribute("activeMenu", "users");
        return "admin/users/form";
    }

    @PostMapping
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (isBlank(user.getFullName()) || isBlank(user.getEmail()) || isBlank(user.getUsername()) || isBlank(user.getPassword()) || isBlank(user.getPhone())) {
                redirectAttributes.addFlashAttribute("errorMsg", "All required fields must be filled");
                return "redirect:/admin/users/new";
            }

            if (userRepository.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Username already exists");
                return "redirect:/admin/users/new";
            }

            if (userRepository.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Email already exists");
                return "redirect:/admin/users/new";
            }

            if (userRepository.existsByPhone(user.getPhone())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Phone already exists");
                return "redirect:/admin/users/new";
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("flashMsg", "User added successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error adding user: " + e.getMessage());
            return "redirect:/admin/users/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "User not found");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", userOpt.get());
        model.addAttribute("title", "Edit User · Admin");
        model.addAttribute("pageHeading", "Edit User");
        model.addAttribute("activeMenu", "users");
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String editUser(@PathVariable String id, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "User not found");
                return "redirect:/admin/users";
            }

            User existingUser = existingUserOpt.get();
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setCompanyName(user.getCompanyName());
            existingUser.setUsername(user.getUsername());
            existingUser.setStatus(user.getStatus());
            existingUser.setInternalNotes(user.getInternalNotes());

            if (!isBlank(user.getPassword())) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            userRepository.save(existingUser);
            redirectAttributes.addFlashAttribute("flashMsg", "User updated successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "User not found");
                return "redirect:/admin/users";
            }
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("flashMsg", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}




