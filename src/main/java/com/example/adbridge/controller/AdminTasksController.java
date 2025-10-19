package com.example.adbridge.controller;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Task;
import com.example.adbridge.model.User;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.TaskRepository;
import com.example.adbridge.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTasksController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model, HttpSession session) {
        List<Task> tasks = taskRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim().toLowerCase();
            tasks = tasks.stream()
                    .filter(task -> 
                        (task.getTitle() != null && task.getTitle().toLowerCase().contains(searchTerm)) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchTerm)) ||
                        (task.getAssignedTo() != null && task.getAssignedTo().toLowerCase().contains(searchTerm)) ||
                        (task.getPriority() != null && task.getPriority().toString().toLowerCase().contains(searchTerm)) ||
                        (task.getStatus() != null && task.getStatus().toString().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        
        // Apply status filter
        if (status != null && !status.trim().isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus() != null && 
                                task.getStatus().toString().equalsIgnoreCase(status))
                    .toList();
        }

        List<User> users = userRepository.findAll();

        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("title", "Tasks · Admin");
        model.addAttribute("pageHeading", "Task Management");
        model.addAttribute("activeMenu", "tasks");
        return "admin/tasks/list";
    }

    @GetMapping("/new")
    public String newTask(Model model, HttpSession session) {
        List<String> staffRoles = Arrays.asList("MANAGING_DIRECTOR", "MARKETING_PLANNER", "ADMIN_ASSISTANT", "FINANCE_COORDINATOR", "CLIENT_SUPPORT_OFFICER", "SENIOR_WEB_DEVELOPER");

        model.addAttribute("task", new Task());
        model.addAttribute("staffRoles", staffRoles);
        model.addAttribute("title", "New Task · Admin");
        model.addAttribute("pageHeading", "Create New Task");
        model.addAttribute("activeMenu", "tasks");
        return "admin/tasks/form";
    }

    // New method for creating a task associated with a booking
    @GetMapping("/new/{bookingId}")
    public String newTaskForCampaign(@PathVariable Long bookingId, Model model, HttpSession session, RedirectAttributes ra) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            ra.addFlashAttribute("errorMsg", "Booking not found!");
            return "redirect:/admin/campaigns";
        }

        Task task = new Task();
        task.setBooking(booking);

        // Calculate campaign deadline
        LocalDate bookedOn = booking.getLastModifiedAt() != null ? booking.getLastModifiedAt().toLocalDate() : LocalDate.now();
        LocalDate campaignDeadline = null;
        int daysToAdd = 0;

        switch (booking.getServiceType()) {
            case TV_ADVERTISEMENT:
            case BILLBOARD_ADVERTISING:
                daysToAdd = 30;
                break;
            case RADIO_PROMOTION:
                daysToAdd = 21;
                break;
            case SOCIAL_MEDIA_CAMPAIGN:
            case DIGITAL_BANNER:
                daysToAdd = 7;
                break;
            case NEWSPAPER_AD:
                daysToAdd = 2;
                break;
        }

        if (daysToAdd > 0) {
            campaignDeadline = bookedOn.plusDays(daysToAdd);
        }

        List<String> staffRoles = Arrays.asList("MANAGING_DIRECTOR", "MARKETING_PLANNER", "ADMIN_ASSISTANT", "FINANCE_COORDINATOR", "CLIENT_SUPPORT_OFFICER", "SENIOR_WEB_DEVELOPER");

        model.addAttribute("task", task);
        model.addAttribute("staffRoles", staffRoles);
        model.addAttribute("bookedOn", bookedOn);
        model.addAttribute("campaignDeadline", campaignDeadline);
        model.addAttribute("title", "New Task · Admin");
        model.addAttribute("pageHeading", "Create New Task for Campaign: " + booking.getBookingId());
        model.addAttribute("activeMenu", "campaigns");
        return "admin/tasks/form";
    }

    @PostMapping
    public String create(@ModelAttribute Task task,
                         @RequestParam("assignedTo") String assignedTo,
                         HttpSession session,
                         RedirectAttributes ra) {
        try {
            List<String> validRoles = Arrays.asList("MANAGING_DIRECTOR", "MARKETING_PLANNER", "ADMIN_ASSISTANT", "FINANCE_COORDINATOR", "CLIENT_SUPPORT_OFFICER", "SENIOR_WEB_DEVELOPER");

            if (!validRoles.contains(assignedTo)) {
                ra.addFlashAttribute("errorMsg", "Invalid staff role selected");
                return "redirect:/admin/tasks/new";
            }

            task.setAssignedTo(assignedTo);
            task.setAssignedBy("Admin"); // Session logic to be added
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());

            // If there's a booking associated with the task, fetch it
            if (task.getBooking() != null && task.getBooking().getId() != null) {
                Booking booking = bookingRepository.findById(task.getBooking().getId()).orElse(null);
                task.setBooking(booking);
            }

            taskRepository.save(task);

            ra.addFlashAttribute("flashMsg", "Task created successfully");

            // Redirect based on whether a booking is associated
            if (task.getBooking() != null) {
                return "redirect:/admin/campaigns/" + task.getBooking().getId();
            } else {
                return "redirect:/admin/tasks";
            }

        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error creating task: " + e.getMessage());
            if (task.getBooking() != null && task.getBooking().getId() != null) {
                return "redirect:/admin/tasks/new/" + task.getBooking().getId();
            }
            return "redirect:/admin/tasks/new";
        }
    }
}
