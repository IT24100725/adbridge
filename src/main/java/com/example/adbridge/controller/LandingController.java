package com.example.adbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class LandingController {

    @GetMapping("/")
    public String landing(Model model) {
        // Quick, in-memory services list for homepage rendering
        List<Map<String, String>> services = List.of(
                Map.of(
                        "title", "TV Advertisements",
                        "desc", "Show your brand on popular TV channels across Sri Lanka."),
                Map.of(
                        "title", "Radio Promotions",
                        "desc", "Reach local audiences with catchy radio ads."),
                Map.of(
                        "title", "Social Media Campaigns",
                        "desc", "Advertise on Facebook, Instagram, and TikTok."),
                Map.of(
                        "title", "Newspaper Ads",
                        "desc", "Place your ad in top newspapers for wide reach."),
                Map.of(
                        "title", "Billboard Advertising",
                        "desc", "Display your ad in busy city areas."),
                Map.of(
                        "title", "Digital Banners",
                        "desc", "Show ads on websites and mobile apps.")
        );

        model.addAttribute("services", services);
        
        
        return "index";
    }

    @GetMapping("/services")
    public String servicesPage(Model model) {
        model.addAttribute("title", "Services");
        model.addAttribute("showBookInNav", true);
        model.addAttribute("showPaymentInNav", false);
        model.addAttribute("message", "Welcome to the services page");
        return "services";
    }
}