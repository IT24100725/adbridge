package com.example.adbridge.controller;

import com.example.adbridge.model.Booking;
import com.example.adbridge.model.Invoice;
import com.example.adbridge.repo.BookingRepository;
import com.example.adbridge.repo.InvoiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/invoices")
public class AdminInvoicesController {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

    public AdminInvoicesController(InvoiceRepository invoiceRepository, BookingRepository bookingRepository) {
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                      @RequestParam(value = "status", required = false) Invoice.Status status,
                      @RequestParam(value = "client", required = false) String client,
                      @RequestParam(value = "amountMin", required = false) Integer amountMin,
                      @RequestParam(value = "amountMax", required = false) Integer amountMax,
                      Model model) {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        // Apply search filter
        if (q != null && !q.trim().isEmpty()) {
            String qq = q.trim().toLowerCase();
            invoices = invoices.stream().filter(i ->
                    (i.getInvoiceId() != null && i.getInvoiceId().toLowerCase().contains(qq)) ||
                    (i.getBookingId() != null && i.getBookingId().toLowerCase().contains(qq)) ||
                    (i.getClientName() != null && i.getClientName().toLowerCase().contains(qq)) ||
                    (i.getClientEmail() != null && i.getClientEmail().toLowerCase().contains(qq))
            ).toList();
        }
        
        // Apply status filter
        if (status != null) {
            invoices = invoices.stream().filter(i -> i.getStatus() == status).toList();
        }
        
        // Apply client filter
        if (client != null && !client.trim().isEmpty()) {
            String clientFilter = client.trim().toLowerCase();
            invoices = invoices.stream().filter(i ->
                    (i.getClientName() != null && i.getClientName().toLowerCase().contains(clientFilter)) ||
                    (i.getClientEmail() != null && i.getClientEmail().toLowerCase().contains(clientFilter))
            ).toList();
        }
        
        // Apply amount range filter
        if (amountMin != null) {
            invoices = invoices.stream().filter(i -> i.getAmount() >= amountMin).toList();
        }
        if (amountMax != null) {
            invoices = invoices.stream().filter(i -> i.getAmount() <= amountMax).toList();
        }
        
        model.addAttribute("title", "Invoices · Admin");
        model.addAttribute("pageHeading", "Invoices");
        model.addAttribute("activeMenu", "invoices");
        model.addAttribute("invoices", invoices);
        model.addAttribute("q", q);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentClient", client);
        model.addAttribute("currentAmountMin", amountMin);
        model.addAttribute("currentAmountMax", amountMax);
        model.addAttribute("statusOptions", Invoice.Status.values());
        return "admin/invoices/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(value = "bookingId", required = false) String bookingId, Model model) {
        model.addAttribute("title", "New Invoice · Admin");
        model.addAttribute("activeMenu", "invoices");
        model.addAttribute("bookingId", bookingId);
        return "admin/invoices/form";
    }

    @PostMapping
    public String create(@RequestParam String bookingId,
                         @RequestParam Integer amount,
                         @RequestParam(value = "dueDate", required = false) String dueDate,
                         @RequestParam(value = "notes", required = false) String notes,
                         RedirectAttributes ra) {
        Optional<Booking> b = bookingRepository.findByBookingId(bookingId);
        if (b.isEmpty()) {
            ra.addFlashAttribute("error", "Booking not found");
            return "redirect:/admin/invoices/new";
        }
        Booking booking = b.get();
        Invoice inv = new Invoice();
        inv.setInvoiceId("INV-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        inv.setBookingId(bookingId);
        inv.setClientName(booking.getFullName());
        inv.setClientEmail(booking.getEmail());
        inv.setAmount(amount);
        inv.setCurrency("LKR");
        inv.setStatus(Invoice.Status.ISSUED);
        if (dueDate != null && !dueDate.isBlank()) {
            try { inv.setDueDate(LocalDateTime.parse(dueDate)); } catch (Exception ignored) {}
        }
        inv.setNotes(notes);
        invoiceRepository.save(inv);
        ra.addFlashAttribute("success", "Invoice created");
        return "redirect:/admin/invoices";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Invoice> inv = invoiceRepository.findById(id);
        if (inv.isEmpty()) {
            ra.addFlashAttribute("error", "Invoice not found");
            return "redirect:/admin/invoices";
        }
        model.addAttribute("title", "Invoice · Admin");
        model.addAttribute("activeMenu", "invoices");
        model.addAttribute("invoice", inv.get());
        return "admin/invoices/detail";
    }

    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id, RedirectAttributes ra) {
        invoiceRepository.findById(id).ifPresent(inv -> {
            inv.setStatus(Invoice.Status.PAID);
            invoiceRepository.save(inv);
        });
        ra.addFlashAttribute("success", "Invoice marked as PAID");
        return "redirect:/admin/invoices";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        invoiceRepository.findById(id).ifPresent(inv -> {
            inv.setStatus(Invoice.Status.CANCELLED);
            invoiceRepository.save(inv);
        });
        ra.addFlashAttribute("success", "Invoice cancelled");
        return "redirect:/admin/invoices";
    }
}






