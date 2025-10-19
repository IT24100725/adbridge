package com.example.adbridge.controller;

import com.example.adbridge.model.Payment;
import com.example.adbridge.model.Task;
import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Budget;
import com.example.adbridge.model.Expense;
import com.example.adbridge.model.User;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.PaymentRepository;
import com.example.adbridge.repo.TaskRepository;
import com.example.adbridge.repo.UserRepository;
import com.example.adbridge.repo.BudgetRepository;
import com.example.adbridge.repo.ExpenseRepository;
import com.example.adbridge.service.SimpleReportService;
import com.example.adbridge.service.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminReportsController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TaskRepository taskRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    private SimpleReportService simpleReportService;

    @Autowired
    private FinancialReportService financialReportService;

    private LocalDate parseDateSafely(String dateString, LocalDate defaultDate) {
        try {
            return (dateString != null && !dateString.trim().isEmpty()) ?
                    LocalDate.parse(dateString) : defaultDate;
        } catch (Exception e) {
            return defaultDate;
        }
    }

    public AdminReportsController(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  PaymentRepository paymentRepository,
                                  TaskRepository taskRepository,
                                  BudgetRepository budgetRepository,
                                  ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.taskRepository = taskRepository;
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(value = "dateFrom", required = false) String dateFrom,
                          @RequestParam(value = "dateTo", required = false) String dateTo,
                          @RequestParam(value = "reportType", required = false) String reportType,
                          Model model) {

        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());

        boolean reportGenerated = reportType != null && !reportType.trim().isEmpty();

        if (reportGenerated) {
            switch (reportType) {
                case "revenue":
                    model.addAttribute("monthlyRevenue", calculateMonthlyRevenue(startDate, endDate));
                    List<Payment> revenuePayments = paymentRepository.findByPaymentStatus(Payment.PaymentStatus.COMPLETED).stream()
                            .filter(p -> p.getCreatedAt() != null &&
                                    p.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                                    p.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                            .collect(Collectors.toList());
                    model.addAttribute("revenueDetails", revenuePayments);
                    break;
                case "campaigns":
                    model.addAttribute("campaignPerformance", calculateCampaignPerformance(startDate, endDate));
                    model.addAttribute("campaignDetails", bookingRepository.findAll().stream()
                            .filter(b -> b.getLastModifiedAt() != null && b.getLastModifiedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                                    b.getLastModifiedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                            .collect(Collectors.toList()));
                    break;
                case "tasks":
                    model.addAttribute("taskProgress", calculateTaskProgress(startDate, endDate));
                    model.addAttribute("taskDetails", taskRepository.findAll().stream()
                            .filter(t -> t.getCreatedAt() != null &&
                                    t.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                                    t.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                            .collect(Collectors.toList()));
                    break;
                case "users":
                    model.addAttribute("userActivity", calculateUserActivity(startDate, endDate));
                    model.addAttribute("userDetails", userRepository.findAll()); // No date filtering
                    break;
                case "payments":
                    model.addAttribute("paymentTrends", calculatePaymentTrends(startDate, endDate));
                    model.addAttribute("paymentDetails", paymentRepository.findAll().stream()
                            .filter(p -> p.getCreatedAt() != null &&
                                    p.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                                    p.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                            .collect(Collectors.toList()));
                    break;
                case "clients":
                    model.addAttribute("clientDetails", generateClientReport(startDate, endDate));
                    break;
            }
        }

        model.addAttribute("title", "Reports · Admin");
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageHeading", "Reports");
        model.addAttribute("reportGenerated", reportGenerated);
        model.addAttribute("reportType", reportType);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/reports/index";
    }

    private List<Map<String, Object>> generateClientReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getLastModifiedAt() != null && b.getLastModifiedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                        b.getLastModifiedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        Map<String, List<Booking>> bookingsByUser = bookings.stream()
                .filter(b -> b.getEmail() != null)
                .collect(Collectors.groupingBy(Booking::getEmail));

        List<Map<String, Object>> clientDetails = new ArrayList<>();

        for (Map.Entry<String, List<Booking>> entry : bookingsByUser.entrySet()) {
            String userEmail = entry.getKey();
            List<Booking> userBookings = entry.getValue();
            User user = userRepository.findByEmail(userEmail).stream().findFirst().orElse(null);

            if (user != null) {
                Map<String, Object> detail = new HashMap<>();
                long totalCampaigns = userBookings.size();
                double totalSpent = userBookings.stream()
                        .filter(b -> "COMPLETED".equalsIgnoreCase(b.getPaymentStatus()))
                        .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                        .sum();
                String favoriteService = userBookings.stream()
                        .filter(b -> b.getServiceType() != null)
                        .collect(Collectors.groupingBy(b -> b.getServiceType().name(), Collectors.counting()))
                        .entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A");
                LocalDate lastCampaignDate = userBookings.stream()
                        .map(b -> b.getLastModifiedAt().toLocalDate())
                        .max(LocalDate::compareTo)
                        .orElse(null);

                detail.put("clientName", user.getFullName());
                detail.put("totalCampaigns", totalCampaigns);
                detail.put("totalSpent", totalSpent);
                detail.put("favoriteService", favoriteService);
                detail.put("lastCampaignDate", lastCampaignDate);
                clientDetails.add(detail);
            }
        }
        return clientDetails;
    }

    private Map<String, Object> calculateMonthlyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> revenue = new HashMap<>();
        List<Payment> completedPayments = paymentRepository.findByPaymentStatus(Payment.PaymentStatus.COMPLETED);

        double currentPeriodRevenue = completedPayments.stream()
                .filter(p -> p.getCreatedAt() != null &&
                        !p.getCreatedAt().toLocalDate().isBefore(startDate) &&
                        p.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0)
                .sum();

        LocalDate prevStartDate = startDate.minusMonths(1);
        LocalDate prevEndDate = endDate.minusMonths(1);
        double previousPeriodRevenue = completedPayments.stream()
                .filter(p -> p.getCreatedAt() != null &&
                        !p.getCreatedAt().toLocalDate().isBefore(prevStartDate) &&
                        p.getCreatedAt().toLocalDate().isBefore(prevEndDate.plusDays(1)))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0)
                .sum();

        double totalRevenue = completedPayments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0).sum();

        double growthRate = 0;
        if (previousPeriodRevenue > 0) {
            growthRate = ((currentPeriodRevenue - previousPeriodRevenue) / previousPeriodRevenue) * 100;
        }

        revenue.put("totalRevenue", totalRevenue);
        revenue.put("currentPeriod", currentPeriodRevenue);
        revenue.put("previousPeriod", previousPeriodRevenue);
        revenue.put("growthRate", growthRate);
        revenue.put("growthDirection", currentPeriodRevenue >= previousPeriodRevenue ? "up" : "down");
        return revenue;
    }

    private Map<String, Object> calculateCampaignPerformance(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> performance = new HashMap<>();
        List<Booking> campaignsInPeriod = bookingRepository.findAll().stream()
                .filter(b -> b.getLastModifiedAt() != null &&
                        !b.getLastModifiedAt().toLocalDate().isBefore(startDate) &&
                        b.getLastModifiedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        long totalCampaigns = campaignsInPeriod.size();
        long approvedCampaigns = campaignsInPeriod.stream().filter(b -> "APPROVED".equalsIgnoreCase(b.getCampaignStatus())).count();
        long pendingCampaigns = campaignsInPeriod.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getCampaignStatus())).count();
        double approvalRate = totalCampaigns > 0 ? ((double) approvedCampaigns / totalCampaigns) * 100 : 0;

        performance.put("totalCampaigns", totalCampaigns);
        performance.put("approvedCampaigns", approvedCampaigns);
        performance.put("pendingCampaigns", pendingCampaigns);
        performance.put("approvalRate", approvalRate);
        return performance;
    }

    private Map<String, Object> calculateTaskProgress(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> progress = new HashMap<>();
        List<Task> tasksInPeriod = taskRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null &&
                        !t.getCreatedAt().toLocalDate().isBefore(startDate) &&
                        t.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        long totalTasks = tasksInPeriod.size();
        long completedTasks = tasksInPeriod.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long inProgressTasks = tasksInPeriod.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        double completionRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100 : 0;

        progress.put("totalTasks", totalTasks);
        progress.put("completedTasks", completedTasks);
        progress.put("inProgressTasks", inProgressTasks);
        progress.put("completionRate", completionRate);
        return progress;
    }

    private Map<String, Object> calculateUserActivity(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> activity = new HashMap<>();
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();

        long newUsers = allUsers.stream()
                .filter(u -> u.getApprovalStatus() == User.ApprovalStatus.PENDING)
                .count();
        long activeUsers = totalUsers - newUsers;

        double activityRate = totalUsers > 0 ? ((double) activeUsers / totalUsers) * 100 : 0;

        activity.put("totalUsers", totalUsers);
        activity.put("activeUsers", activeUsers);
        activity.put("newUsers", newUsers);
        activity.put("activityRate", activityRate);
        return activity;
    }

    private Map<String, Object> calculatePaymentTrends(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trends = new HashMap<>();
        List<Payment> paymentsInPeriod = paymentRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null &&
                        !p.getCreatedAt().toLocalDate().isBefore(startDate) &&
                        p.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        long totalPayments = paymentsInPeriod.size();
        long successfulPayments = paymentsInPeriod.stream().filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED).count();
        double successRate = totalPayments > 0 ? ((double) successfulPayments / totalPayments) * 100 : 0;
        double avgPaymentAmount = paymentsInPeriod.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0)
                .average().orElse(0.0);

        trends.put("totalPayments", totalPayments);
        trends.put("successfulPayments", successfulPayments);
        trends.put("successRate", successRate);
        trends.put("avgPaymentAmount", avgPaymentAmount);
        return trends;
    }

    // Download endpoints
    @GetMapping("/admin/reports/download/revenue")
    public ModelAndView downloadRevenueReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        Map<String, Object> revenueData = calculateMonthlyRevenue(startDate, endDate);
        return simpleReportService.generateRevenueReportView(revenueData, startDate, endDate);
    }

    @GetMapping("/admin/reports/download/campaigns")
    public ModelAndView downloadCampaignReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        Map<String, Object> campaignData = calculateCampaignPerformance(startDate, endDate);
        return simpleReportService.generateCampaignReportView(campaignData, startDate, endDate);
    }

    @GetMapping("/admin/reports/download/tasks")
    public ModelAndView downloadTaskReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        Map<String, Object> taskData = calculateTaskProgress(startDate, endDate);
        return simpleReportService.generateTaskReportView(taskData, startDate, endDate);
    }

    @GetMapping("/admin/reports/download/users")
    public ModelAndView downloadUserReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        Map<String, Object> userData = calculateUserActivity(startDate, endDate);
        return simpleReportService.generateUserReportView(userData, startDate, endDate);
    }

    @GetMapping("/admin/reports/download/payments")
    public ModelAndView downloadPaymentReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        Map<String, Object> paymentData = calculatePaymentTrends(startDate, endDate);
        return simpleReportService.generatePaymentReportView(paymentData, startDate, endDate);
    }

    @GetMapping("/admin/reports/download/clients")
    public ModelAndView downloadClientReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo) {
        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());
        List<Map<String, Object>> clientDetails = generateClientReport(startDate, endDate);
        return simpleReportService.generateClientReportView(clientDetails, startDate, endDate);
    }


    @GetMapping("/admin/reports/financial")
    public String financialReports(@RequestParam(value = "dateFrom", required = false) String dateFrom,
                                   @RequestParam(value = "dateTo", required = false) String dateTo,
                                   @RequestParam(value = "format", required = false) String format,
                                   Model model) {

        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());

        List<Budget> approvedBudgets = budgetRepository.findAll().stream()
                .filter(b -> b.getApprovalStatus() == Budget.ApprovalStatus.APPROVED && b.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) && b.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        List<Expense> approvedExpenses = expenseRepository.findAll().stream()
                .filter(e -> e.getApprovalStatus() == Expense.ApprovalStatus.APPROVED && e.getExpenseDate().isAfter(startDate.minusDays(1)) && e.getExpenseDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        double totalBudgetAmount = approvedBudgets.stream().mapToDouble(Budget::getAmount).sum();
        double totalExpenseAmount = approvedExpenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<Expense.Category, Double> categoryExpenses = approvedExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        model.addAttribute("title", "Financial Reports · Admin");
        model.addAttribute("pageHeading", "Financial Reports");
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("approvedBudgets", approvedBudgets);
        model.addAttribute("approvedExpenses", approvedExpenses);
        model.addAttribute("totalBudgetAmount", totalBudgetAmount);
        model.addAttribute("totalExpenseAmount", totalExpenseAmount);
        model.addAttribute("categoryExpenses", categoryExpenses);
        model.addAttribute("format", format);

        return "admin/reports/financial";
    }

    @GetMapping("/admin/reports/financial/pdf")
    public ResponseEntity<byte[]> downloadFinancialPdfReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("ADMIN_LOGGED_IN"))) {
            return ResponseEntity.status(403).build();
        }

        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());

        List<Budget> approvedBudgets = budgetRepository.findAll().stream()
                .filter(b -> b.getApprovalStatus() == Budget.ApprovalStatus.APPROVED && b.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) && b.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        List<Expense> approvedExpenses = expenseRepository.findAll().stream()
                .filter(e -> e.getApprovalStatus() == Expense.ApprovalStatus.APPROVED && e.getExpenseDate().isAfter(startDate.minusDays(1)) && e.getExpenseDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        byte[] pdfBytes = financialReportService.generateFinancialPdfReport(approvedBudgets, approvedExpenses, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "financial_report.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/admin/reports/financial/csv")
    public void downloadFinancialCsvReport(@RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo, HttpSession session, HttpServletResponse response) throws Exception {
        if (!Boolean.TRUE.equals(session.getAttribute("ADMIN_LOGGED_IN"))) {
            response.sendRedirect("/admin/login");
            return;
        }

        LocalDate startDate = parseDateSafely(dateFrom, LocalDate.now().minusMonths(1));
        LocalDate endDate = parseDateSafely(dateTo, LocalDate.now());

        List<Budget> approvedBudgets = budgetRepository.findAll().stream()
                .filter(b -> b.getApprovalStatus() == Budget.ApprovalStatus.APPROVED && b.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) && b.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        List<Expense> approvedExpenses = expenseRepository.findAll().stream()
                .filter(e -> e.getApprovalStatus() == Expense.ApprovalStatus.APPROVED && e.getExpenseDate().isAfter(startDate.minusDays(1)) && e.getExpenseDate().isBefore(endDate.plusDays(1)))
                .collect(Collectors.toList());

        response.setContentType("text/csv");
        String fileName = "financial_report_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        PrintWriter writer = response.getWriter();
        financialReportService.generateFinancialCsvReport(approvedBudgets, approvedExpenses, startDate, endDate, writer);
    }
}
