package com.example.adbridge.service;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Payment;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignPdfService {

    public byte[] generateApprovedCampaignsReport(List<Booking> campaigns) {
        String html = generateApprovedCampaignsHtml(campaigns);
        return convertHtmlToPdf(html);
    }

    public byte[] generateRejectedCampaignsReport(List<Booking> campaigns) {
        String html = generateRejectedCampaignsHtml(campaigns);
        return convertHtmlToPdf(html);
    }

    private String generateApprovedCampaignsHtml(List<Booking> campaigns) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Approved Campaigns Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 10px; font-size: 10px; }");
        html.append("h1 { color: #2c3e50; text-align: center; font-size: 18px; }");
        html.append("h2 { color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 5px; font-size: 14px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 9px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 8px; }");
        html.append("th { background-color: #3498db; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".status-approved { color: #27ae60; font-weight: bold; }");
        html.append(".status-completed { color: #27ae60; font-weight: bold; }");
        html.append(".footer { margin-top: 30px; text-align: center; color: #7f8c8d; font-size: 8px; }");
        html.append("@page { size: A4 landscape; margin: 0.5in; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>Approved Campaigns Report</h1>");
        html.append("<p><strong>Generated on:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
        html.append("<p><strong>Total Approved Campaigns:</strong> " + campaigns.size() + "</p>");
        
        html.append("<h2>Campaign Details</h2>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Campaign ID</th>");
        html.append("<th>Client Name</th>");
        html.append("<th>Email</th>");
        html.append("<th>Service Type</th>");
        html.append("<th>Total Amount</th>");
        html.append("<th>Payment Status</th>");
        html.append("<th>Campaign Status</th>");
        html.append("<th>Created Date</th>");
        html.append("</tr>");
        
        for (Booking campaign : campaigns) {
            html.append("<tr>");
            html.append("<td>").append(campaign.getId()).append("</td>");
            html.append("<td>").append(campaign.getFullName()).append("</td>");
            html.append("<td>").append(campaign.getEmail()).append("</td>");
            html.append("<td>").append(campaign.getServiceType()).append("</td>");
            html.append("<td>").append(campaign.getTotalAmount()).append("</td>");
            html.append("<td class='status-completed'>").append(campaign.getPaymentStatus()).append("</td>");
            html.append("<td class='status-approved'>").append(campaign.getCampaignStatus()).append("</td>");
            html.append("<td>").append(campaign.getLastModifiedAt() != null ? 
                campaign.getLastModifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<div class='footer'>");
        html.append("<p>AdBridgeLanka - Marketing Department</p>");
        html.append("<p>This report contains all approved campaigns by Managing Director</p>");
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }

    private String generateRejectedCampaignsHtml(List<Booking> campaigns) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Rejected Campaigns Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 10px; font-size: 10px; }");
        html.append("h1 { color: #2c3e50; text-align: center; font-size: 18px; }");
        html.append("h2 { color: #34495e; border-bottom: 2px solid #e74c3c; padding-bottom: 5px; font-size: 14px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 9px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; font-size: 8px; }");
        html.append("th { background-color: #e74c3c; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".status-rejected { color: #e74c3c; font-weight: bold; }");
        html.append(".footer { margin-top: 30px; text-align: center; color: #7f8c8d; font-size: 8px; }");
        html.append("@page { size: A4 landscape; margin: 0.5in; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>Rejected Campaigns Report</h1>");
        html.append("<p><strong>Generated on:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
        html.append("<p><strong>Total Rejected Campaigns:</strong> " + campaigns.size() + "</p>");
        
        html.append("<h2>Campaign Details</h2>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Campaign ID</th>");
        html.append("<th>Client Name</th>");
        html.append("<th>Email</th>");
        html.append("<th>Service Type</th>");
        html.append("<th>Total Amount</th>");
        html.append("<th>Payment Status</th>");
        html.append("<th>Campaign Status</th>");
        html.append("<th>Rejected Date</th>");
        html.append("</tr>");
        
        for (Booking campaign : campaigns) {
            html.append("<tr>");
            html.append("<td>").append(campaign.getId()).append("</td>");
            html.append("<td>").append(campaign.getFullName()).append("</td>");
            html.append("<td>").append(campaign.getEmail()).append("</td>");
            html.append("<td>").append(campaign.getServiceType()).append("</td>");
            html.append("<td>").append(campaign.getTotalAmount()).append("</td>");
            html.append("<td>").append(campaign.getPaymentStatus()).append("</td>");
            html.append("<td class='status-rejected'>").append(campaign.getCampaignStatus()).append("</td>");
            html.append("<td>").append(campaign.getLastModifiedAt() != null ? 
                campaign.getLastModifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<div class='footer'>");
        html.append("<p>AdBridgeLanka - Marketing Department</p>");
        html.append("<p>This report contains all rejected campaigns by Marketing Planner or Managing Director</p>");
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
