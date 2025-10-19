package com.example.adbridge.service;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class SimpleReportService {

    public ModelAndView generateRevenueReportView(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/revenue-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Revenue Report");

        return modelAndView;
    }

    public ModelAndView generateCampaignReportView(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/campaign-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Campaign Performance Report");

        return modelAndView;
    }

    public ModelAndView generateTaskReportView(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/task-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Task Progress Report");

        return modelAndView;
    }

    public ModelAndView generateUserReportView(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/user-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "User Activity Report");

        return modelAndView;
    }

    public ModelAndView generatePaymentReportView(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/payment-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Payment Trends Report");

        return modelAndView;
    }

    public ModelAndView generateClientReportView(List<Map<String, Object>> clientDetails, LocalDate startDate, LocalDate endDate) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/client-pdf");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("clientDetails", clientDetails);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Client Report");

        return modelAndView;
    }

    public ModelAndView generateFinancialCSVReport(Map<String, Object> reportData) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/financial-csv");

        LocalDate startDate = (LocalDate) reportData.get("startDate");
        LocalDate endDate = (LocalDate) reportData.get("endDate");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Financial Report");

        return modelAndView;
    }

    public ModelAndView generateFinancialPDFReport(Map<String, Object> reportData) {
        ModelAndView modelAndView = new ModelAndView("admin/reports/financial-pdf");

        LocalDate startDate = (LocalDate) reportData.get("startDate");
        LocalDate endDate = (LocalDate) reportData.get("endDate");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        String generatedTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        modelAndView.addObject("reportData", reportData);
        modelAndView.addObject("period", period);
        modelAndView.addObject("generatedTime", generatedTime);
        modelAndView.addObject("reportType", "Financial Report");

        return modelAndView;
    }

}
