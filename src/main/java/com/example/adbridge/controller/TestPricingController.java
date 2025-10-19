package com.example.adbridge.controller;

import com.example.adbridge.service.PricingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPricingController {
    
    private final PricingService pricingService;
    
    public TestPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }
    
    @GetMapping("/test-pricing")
    public String testPricing(Model model) {
        // Test Radio Promotion
        int radioPrice = pricingService.calculateRadioPromotion("morning", 15, 10);
        
        // Test TV Advertisement
        int tvPrice = pricingService.calculateTVAdvertisement("evening", 30, 7);
        
        // Test Social Media
        String[] platforms = {"Facebook", "Instagram"};
        String[] contentTypes = {"Video"};
        int socialPrice = pricingService.calculateSocialMediaCampaign(platforms, contentTypes, 14);
        
        model.addAttribute("radioPrice", radioPrice);
        model.addAttribute("tvPrice", tvPrice);
        model.addAttribute("socialPrice", socialPrice);
        
        return "test-pricing";
    }
}
