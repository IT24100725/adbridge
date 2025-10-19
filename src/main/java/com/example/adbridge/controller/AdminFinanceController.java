package com.example.adbridge.controller;

import com.example.adbridge.model.Budget;
import com.example.adbridge.model.Expense;
import com.example.adbridge.model.AdminRole;
import com.example.adbridge.repo.BudgetRepository;
import com.example.adbridge.repo.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/finance")
public class AdminFinanceController {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public AdminFinanceController(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }
    
    // Helper method for safe date parsing
    private LocalDate parseDateSafely(String dateString, LocalDate defaultDate) {
        try {
            return (dateString != null && !dateString.trim().isEmpty()) ? 
                   LocalDate.parse(dateString) : defaultDate;
        } catch (Exception e) {
            return defaultDate;
        }
    }

    @GetMapping
    public String financeHome(@RequestParam(value = "q", required = false) String q,
                             @RequestParam(value = "category", required = false) Expense.Category category,
                             @RequestParam(value = "dateFrom", required = false) String dateFrom,
                             @RequestParam(value = "dateTo", required = false) String dateTo,
                             @RequestParam(value = "amountMin", required = false) Integer amountMin,
                             @RequestParam(value = "amountMax", required = false) Integer amountMax,
                             Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user has permission to access finance (only Admin Assistant, Finance Coordinator, and Super Admin)
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == null || (role != AdminRole.ADMIN && role != AdminRole.FINANCE && role != AdminRole.SUPER_ADMIN)) {
            ra.addFlashAttribute("error", "Access denied. You don't have permission to view finance information.");
            return "redirect:/admin/finance";
        }
        
        // Filter data based on user role
        List<Expense> expenses;
        List<Budget> budgets;
        
        if (role == AdminRole.ADMIN) {
            // Admin Assistant - see approved items + their own pending items
            expenses = expenseRepository.findAll().stream()
                .filter(e -> e.getApprovalStatus() == Expense.ApprovalStatus.APPROVED || 
                           (e.getApprovalStatus() == Expense.ApprovalStatus.PENDING && "ADMIN".equals(e.getCreatedByRole())))
                .collect(Collectors.toList());
            budgets = budgetRepository.findAll().stream()
                .filter(b -> b.getApprovalStatus() == Budget.ApprovalStatus.APPROVED ||
                           (b.getApprovalStatus() == Budget.ApprovalStatus.PENDING && "ADMIN".equals(b.getCreatedByRole())))
                .collect(Collectors.toList());
        } else {
            // Finance Coordinator and Super Admin - see all items
            expenses = expenseRepository.findAll();
            budgets = budgetRepository.findAll();
        }
        
        // Apply search and filters
        if (q != null && !q.trim().isEmpty()) {
            expenses = expenses.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(q.toLowerCase()) ||
                           e.getNotes() != null && e.getNotes().toLowerCase().contains(q.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (category != null) {
            expenses = expenses.stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
        }
        
        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            LocalDate fromDate = parseDateSafely(dateFrom, LocalDate.now().minusYears(1));
            expenses = expenses.stream()
                .filter(e -> e.getExpenseDate().isAfter(fromDate) || e.getExpenseDate().isEqual(fromDate))
                .collect(Collectors.toList());
        }
        
        if (dateTo != null && !dateTo.trim().isEmpty()) {
            LocalDate toDate = parseDateSafely(dateTo, LocalDate.now());
            expenses = expenses.stream()
                .filter(e -> e.getExpenseDate().isBefore(toDate) || e.getExpenseDate().isEqual(toDate))
                .collect(Collectors.toList());
        }
        
        if (amountMin != null) {
            expenses = expenses.stream()
                .filter(e -> e.getAmount() >= amountMin)
                .collect(Collectors.toList());
        }
        
        if (amountMax != null) {
            expenses = expenses.stream()
                .filter(e -> e.getAmount() <= amountMax)
                .collect(Collectors.toList());
        }
        
        // Calculate budget vs actual tracking
        Map<String, Integer> budgetVsActual = calculateBudgetVsActual(budgets, expenses);
        
        // Calculate category-wise spending
        Map<Expense.Category, Integer> categorySpending = expenses.stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.summingInt(Expense::getAmount)
            ));
        
        // Calculate monthly totals
        int currentMonthTotal = expenses.stream()
            .filter(e -> e.getExpenseDate().getMonth() == LocalDate.now().getMonth() &&
                        e.getExpenseDate().getYear() == LocalDate.now().getYear())
            .filter(e -> role == AdminRole.ADMIN ? e.getApprovalStatus() == Expense.ApprovalStatus.APPROVED : true)
            .mapToInt(Expense::getAmount)
            .sum();
        
        model.addAttribute("title", "Finance · Admin");
        model.addAttribute("pageHeading", "Finance");
        model.addAttribute("activeMenu", "finance");
        model.addAttribute("adminRole", role);
        model.addAttribute("expenses", expenses);
        model.addAttribute("budgets", budgets);
        model.addAttribute("expense", new Expense());
        model.addAttribute("budget", new Budget());
        model.addAttribute("budgetVsActual", budgetVsActual);
        model.addAttribute("categorySpending", categorySpending);
        model.addAttribute("currentMonthTotal", currentMonthTotal);
        model.addAttribute("q", q);
        model.addAttribute("currentCategory", category);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("amountMin", amountMin);
        model.addAttribute("amountMax", amountMax);
        model.addAttribute("categoryOptions", Expense.Category.values());
        
        return "admin/finance/index";
    }

    @PostMapping("/expenses")
    public String addExpense(@ModelAttribute Expense expense, RedirectAttributes ra, HttpSession session) {
        if (expense.getTitle() == null || expense.getAmount() == null) {
            ra.addFlashAttribute("error", "Expense title and amount are required");
            return "redirect:/admin/finance";
        }
        
        // Set approval status based on user role
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            // Admin Assistant - needs approval
            expense.setApprovalStatus(Expense.ApprovalStatus.PENDING);
            expense.setCreatedByRole("ADMIN");
            ra.addFlashAttribute("success", "Expense submitted for approval");
        } else if (role == AdminRole.FINANCE || role == AdminRole.SUPER_ADMIN) {
            // Finance Coordinator or Super Admin - auto-approved
            expense.setApprovalStatus(Expense.ApprovalStatus.APPROVED);
            expense.setCreatedByRole(role.name());
            ra.addFlashAttribute("success", "Expense added and approved");
        }
        
        expenseRepository.save(expense);
        return "redirect:/admin/finance";
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        // Check if Admin Assistant is trying to delete an approved item
        if (role == AdminRole.ADMIN) {
            Expense expense = expenseRepository.findById(id).orElse(null);
            if (expense != null && expense.getApprovalStatus() == Expense.ApprovalStatus.APPROVED) {
                ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot delete approved expenses.");
                return "redirect:/admin/finance";
            }
        }
        
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            ra.addFlashAttribute("success", "Expense deleted");
        }
        return "redirect:/admin/finance";
    }

    @PostMapping("/budgets")
    public String addBudget(@ModelAttribute Budget budget, RedirectAttributes ra, HttpSession session) {
        if (budget.getName() == null || budget.getAmount() == null) {
            ra.addFlashAttribute("error", "Budget name and amount are required");
            return "redirect:/admin/finance";
        }
        
        // Set approval status based on user role
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role == AdminRole.ADMIN) {
            // Admin Assistant - needs approval
            budget.setApprovalStatus(Budget.ApprovalStatus.PENDING);
            budget.setCreatedByRole("ADMIN");
            ra.addFlashAttribute("success", "Budget submitted for approval");
        } else if (role == AdminRole.FINANCE || role == AdminRole.SUPER_ADMIN) {
            // Finance Coordinator or Super Admin - auto-approved
            budget.setApprovalStatus(Budget.ApprovalStatus.APPROVED);
            budget.setCreatedByRole(role.name());
            ra.addFlashAttribute("success", "Budget added and approved");
        }
        
        budgetRepository.save(budget);
        return "redirect:/admin/finance";
    }

    @GetMapping("/expenses/{id}/edit")
    public String editExpenseForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense == null) {
            return "redirect:/admin/finance";
        }
        
        // Check if Admin Assistant is trying to edit an approved item
        if (role == AdminRole.ADMIN && expense.getApprovalStatus() == Expense.ApprovalStatus.APPROVED) {
            ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot edit approved expenses.");
            return "redirect:/admin/finance";
        }
        
        model.addAttribute("expense", expense);
        model.addAttribute("categoryOptions", Expense.Category.values());
        return "admin/finance/expense-edit";
    }

    @PostMapping("/expenses/{id}/edit")
    public String updateExpense(@PathVariable Long id, @ModelAttribute Expense expense, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        try {
            Expense existingExpense = expenseRepository.findById(id).orElse(null);
            if (existingExpense == null) {
                ra.addFlashAttribute("error", "Expense not found");
                return "redirect:/admin/finance";
            }
            
            // Check if Admin Assistant is trying to edit an approved item
            if (role == AdminRole.ADMIN && existingExpense.getApprovalStatus() == Expense.ApprovalStatus.APPROVED) {
                ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot edit approved expenses.");
                return "redirect:/admin/finance";
            }
            
            existingExpense.setTitle(expense.getTitle());
            existingExpense.setCategory(expense.getCategory());
            existingExpense.setAmount(expense.getAmount());
            existingExpense.setCurrency(expense.getCurrency());
            existingExpense.setExpenseDate(expense.getExpenseDate());
            existingExpense.setNotes(expense.getNotes());
            
            expenseRepository.save(existingExpense);
            ra.addFlashAttribute("success", "Expense updated successfully");
            return "redirect:/admin/finance";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating expense: " + e.getMessage());
            return "redirect:/admin/finance/expenses/" + id + "/edit";
        }
    }

    @GetMapping("/budgets/{id}/edit")
    public String editBudgetForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget == null) {
            return "redirect:/admin/finance";
        }
        
        // Check if Admin Assistant is trying to edit an approved item
        if (role == AdminRole.ADMIN && budget.getApprovalStatus() == Budget.ApprovalStatus.APPROVED) {
            ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot edit approved budgets.");
            return "redirect:/admin/finance";
        }
        
        model.addAttribute("budget", budget);
        return "admin/finance/budget-edit";
    }

    @PostMapping("/budgets/{id}/edit")
    public String updateBudget(@PathVariable Long id, @ModelAttribute Budget budget, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        try {
            Budget existingBudget = budgetRepository.findById(id).orElse(null);
            if (existingBudget == null) {
                ra.addFlashAttribute("error", "Budget not found");
                return "redirect:/admin/finance";
            }
            
            // Check if Admin Assistant is trying to edit an approved item
            if (role == AdminRole.ADMIN && existingBudget.getApprovalStatus() == Budget.ApprovalStatus.APPROVED) {
                ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot edit approved budgets.");
                return "redirect:/admin/finance";
            }
            
            existingBudget.setName(budget.getName());
            existingBudget.setAmount(budget.getAmount());
            existingBudget.setDescription(budget.getDescription());
            
            budgetRepository.save(existingBudget);
            ra.addFlashAttribute("success", "Budget updated successfully");
            return "redirect:/admin/finance";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating budget: " + e.getMessage());
            return "redirect:/admin/finance/budgets/" + id + "/edit";
        }
    }

    @PostMapping("/budgets/{id}/delete")
    public String deleteBudget(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        
        // Check if Admin Assistant is trying to delete an approved item
        if (role == AdminRole.ADMIN) {
            Budget budget = budgetRepository.findById(id).orElse(null);
            if (budget != null && budget.getApprovalStatus() == Budget.ApprovalStatus.APPROVED) {
                ra.addFlashAttribute("error", "Access denied. Admin Assistant cannot delete approved budgets.");
                return "redirect:/admin/finance";
            }
        }
        
        if (budgetRepository.existsById(id)) {
            budgetRepository.deleteById(id);
            ra.addFlashAttribute("success", "Budget deleted");
        }
        return "redirect:/admin/finance";
    }

    private Map<String, Integer> calculateBudgetVsActual(List<Budget> budgets, List<Expense> expenses) {
        return budgets.stream()
            .collect(Collectors.toMap(
                Budget::getName,
                budget -> {
                    int actualSpent = expenses.stream()
                        .filter(e -> e.getExpenseDate().getMonth() == LocalDate.now().getMonth() &&
                                    e.getExpenseDate().getYear() == LocalDate.now().getYear())
                        .mapToInt(Expense::getAmount)
                        .sum();
                    return budget.getAmount().intValue() - actualSpent;
                },
                (existing, replacement) -> existing + replacement  // Handle duplicate keys by summing
            ));
    }
    
    // Approval methods for Finance Coordinator
    @PostMapping("/expenses/{id}/approve")
    public String approveExpense(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.FINANCE && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Finance Coordinator can approve expenses.");
            return "redirect:/admin/finance";
        }
        
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null) {
            expense.setApprovalStatus(Expense.ApprovalStatus.APPROVED);
            expense.setApprovedBy(role.name());
            expense.setApprovedAt(java.time.LocalDateTime.now());
            expenseRepository.save(expense);
            ra.addFlashAttribute("success", "Expense approved");
        }
        return "redirect:/admin/finance";
    }
    
    @PostMapping("/expenses/{id}/reject")
    public String rejectExpense(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.FINANCE && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Finance Coordinator can reject expenses.");
            return "redirect:/admin/finance";
        }
        
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null) {
            expense.setApprovalStatus(Expense.ApprovalStatus.REJECTED);
            expense.setApprovedBy(role.name());
            expense.setApprovedAt(java.time.LocalDateTime.now());
            expenseRepository.save(expense);
            ra.addFlashAttribute("success", "Expense rejected");
        }
        return "redirect:/admin/finance";
    }
    
    @PostMapping("/budgets/{id}/approve")
    public String approveBudget(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.FINANCE && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Finance Coordinator can approve budgets.");
            return "redirect:/admin/finance";
        }
        
        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget != null) {
            budget.setApprovalStatus(Budget.ApprovalStatus.APPROVED);
            budget.setApprovedBy(role.name());
            budget.setApprovedAt(java.time.LocalDateTime.now());
            budgetRepository.save(budget);
            ra.addFlashAttribute("success", "Budget approved");
        }
        return "redirect:/admin/finance";
    }
    
    @PostMapping("/budgets/{id}/reject")
    public String rejectBudget(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        AdminRole role = (AdminRole) session.getAttribute("ADMIN_ROLE");
        if (role != AdminRole.FINANCE && role != AdminRole.SUPER_ADMIN) {
            ra.addFlashAttribute("error", "Access denied. Only Finance Coordinator can reject budgets.");
            return "redirect:/admin/finance";
        }
        
        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget != null) {
            budget.setApprovalStatus(Budget.ApprovalStatus.REJECTED);
            budget.setApprovedBy(role.name());
            budget.setApprovedAt(java.time.LocalDateTime.now());
            budgetRepository.save(budget);
            ra.addFlashAttribute("success", "Budget rejected");
        }
        return "redirect:/admin/finance";
    }
}






