package com.example.adbridge.service;

import com.example.adbridge.model.Budget;
import com.example.adbridge.model.Expense;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FinancialReportService {

    public byte[] generateFinancialPdfReport(List<Budget> budgets, List<Expense> expenses, LocalDate startDate, LocalDate endDate) {
        String html = generateFinancialPdfHtml(budgets, expenses, startDate, endDate);
        return convertHtmlToPdf(html);
    }

    public void generateFinancialCsvReport(List<Budget> budgets, List<Expense> expenses, LocalDate startDate, LocalDate endDate, PrintWriter writer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        // Write CSV header
        writer.println("Financial Report");
        writer.println("Period," + period);
        writer.println("Generated," + generatedTime);
        writer.println();
        
        // Write approved budgets
        writer.println("APPROVED BUDGETS");
        writer.println("Name,Amount (LKR),Description,Created Date,Created By");
        
        for (Budget budget : budgets) {
            writer.println("\"" + budget.getName() + "\",\"" + 
                         budget.getAmount() + "\",\"" + 
                         (budget.getDescription() != null ? budget.getDescription() : "") + "\",\"" + 
                         budget.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\"" + 
                         "ADMIN\"");
        }
        
        writer.println();
        
        // Write approved expenses
        writer.println("APPROVED EXPENSES");
        writer.println("Title,Category,Amount (LKR),Date,Notes,Created By");
        
        for (Expense expense : expenses) {
            writer.println("\"" + expense.getTitle() + "\",\"" + 
                         expense.getCategory() + "\",\"" + 
                         expense.getAmount() + "\",\"" + 
                         expense.getExpenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\"" + 
                         (expense.getNotes() != null ? expense.getNotes() : "") + "\",\"" + 
                         "ADMIN\"");
        }
    }

    private String generateFinancialPdfHtml(List<Budget> budgets, List<Expense> expenses, LocalDate startDate, LocalDate endDate) {
        StringBuilder html = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Financial Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 10px; font-size: 10px; }");
        html.append("h1 { color: #2c3e50; text-align: center; font-size: 18px; }");
        html.append("h2 { color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 5px; font-size: 14px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 9px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 8px; }");
        html.append("th { background-color: #3498db; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".summary { background-color: #f9f9f9; padding: 15px; margin: 20px 0; border-left: 4px solid #007bff; }");
        html.append(".footer { margin-top: 30px; text-align: center; color: #7f8c8d; font-size: 8px; }");
        html.append("@page { size: A4; margin: 0.5in; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>Financial Report</h1>");
        html.append("<div class='summary'>");
        html.append("<p><strong>Report Period:</strong> " + period + "</p>");
        html.append("<p><strong>Generated On:</strong> " + generatedTime + "</p>");
        html.append("<p><strong>Total Budgets:</strong> " + budgets.size() + " items</p>");
        html.append("<p><strong>Total Expenses:</strong> " + expenses.size() + " items</p>");
        html.append("</div>");
        
        // Calculate totals
        double totalBudgetAmount = budgets.stream().mapToDouble(Budget::getAmount).sum();
        double totalExpenseAmount = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double netAmount = totalBudgetAmount - totalExpenseAmount;
        
        html.append("<div class='summary'>");
        html.append("<p><strong>Total Budget Amount:</strong> LKR " + String.format("%.2f", totalBudgetAmount) + "</p>");
        html.append("<p><strong>Total Expense Amount:</strong> LKR " + String.format("%.2f", totalExpenseAmount) + "</p>");
        html.append("<p><strong>Net Amount:</strong> LKR " + String.format("%.2f", netAmount) + "</p>");
        html.append("</div>");
        
        html.append("<h2>Approved Budgets</h2>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Name</th>");
        html.append("<th>Amount (LKR)</th>");
        html.append("<th>Description</th>");
        html.append("<th>Created Date</th>");
        html.append("<th>Created By</th>");
        html.append("</tr>");
        
        for (Budget budget : budgets) {
            html.append("<tr>");
            html.append("<td>").append(budget.getName()).append("</td>");
            html.append("<td>").append(String.format("%.2f", budget.getAmount().doubleValue())).append("</td>");
            html.append("<td>").append(budget.getDescription() != null ? budget.getDescription() : "").append("</td>");
            html.append("<td>").append(budget.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("</td>");
            html.append("<td>ADMIN</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        
        html.append("<h2>Approved Expenses</h2>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Title</th>");
        html.append("<th>Category</th>");
        html.append("<th>Amount (LKR)</th>");
        html.append("<th>Date</th>");
        html.append("<th>Notes</th>");
        html.append("<th>Created By</th>");
        html.append("</tr>");
        
        for (Expense expense : expenses) {
            html.append("<tr>");
            html.append("<td>").append(expense.getTitle()).append("</td>");
            html.append("<td>").append(expense.getCategory()).append("</td>");
            html.append("<td>").append(String.format("%.2f", expense.getAmount().doubleValue())).append("</td>");
            html.append("<td>").append(expense.getExpenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("</td>");
            html.append("<td>").append(expense.getNotes() != null ? expense.getNotes() : "").append("</td>");
            html.append("<td>ADMIN</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<div class='footer'>");
        html.append("<p>Generated by AdBridgeLanka Financial System</p>");
        html.append("<p>This report contains approved budgets and expenses only</p>");
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }

    private byte[] convertHtmlToPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}

