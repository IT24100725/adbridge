package com.example.adbridge.controller;

import com.example.adbridge.model.AdminRole;
import com.example.adbridge.model.Document;
import com.example.adbridge.repo.DocumentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/documents")
public class AdminDocumentsController {

    @Autowired
    private DocumentRepository documentRepository;

    private static final String UPLOAD_DIR = "uploads/documents/";

    @GetMapping
    public String list(@RequestParam(required = false) String search, 
                       @RequestParam(required = false) String sortBy, 
                       Model model, HttpSession session) {
        List<Document> documents;
        
        if (search != null && !search.trim().isEmpty() && sortBy != null && !sortBy.trim().isEmpty()) {
            // Both search and sort filters
            documents = documentRepository.findByTitleContainingIgnoreCaseAndUploadedByContainingIgnoreCase(search, sortBy);
        } else if (search != null && !search.trim().isEmpty()) {
            // Only search filter
            documents = documentRepository.findByTitleContainingIgnoreCase(search);
        } else if (sortBy != null && !sortBy.trim().isEmpty()) {
            // Only sort filter
            documents = documentRepository.findByUploadedByContainingIgnoreCase(sortBy);
        } else {
            // No filters
            documents = documentRepository.findAll();
        }
        
        model.addAttribute("documents", documents);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("title", "Documents · Admin");
        model.addAttribute("pageHeading", "Document Management");
        model.addAttribute("activeMenu", "documents");
        return "admin/documents/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model, HttpSession session) {
        model.addAttribute("document", new Document());
        model.addAttribute("title", "Upload Document · Admin");
        model.addAttribute("pageHeading", "Upload New Document");
        model.addAttribute("activeMenu", "documents");
        return "admin/documents/upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                        @RequestParam("title") String title,
                        @RequestParam("description") String description,
                        @RequestParam("category") String category,
                        @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                        HttpSession session,
                        RedirectAttributes ra) {
        try {
            if (file.isEmpty()) {
                ra.addFlashAttribute("errorMsg", "Please select a file to upload");
                return "redirect:/admin/documents/upload";
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Create document record
            Document document = new Document();
            document.setTitle(title);
            document.setDescription(description);
            document.setCategory(category);
            document.setFilename(filename);
            document.setFilename2(filename); // Set both filename fields
            document.setFilePath(filePath.toString());
            document.setOriginalFilename(originalFilename);
            document.setOriginalFilename2(originalFilename); // Set both original filename fields
            document.setFileSize(file.getSize());
            document.setFileType(file.getContentType());
            document.setIsPublic(isPublic);
            // Get the current user's role from session
            AdminRole currentRole = (AdminRole) session.getAttribute("ADMIN_ROLE");
            String uploadedByRole = "Admin"; // Default fallback
            
            if (currentRole != null) {
                switch (currentRole) {
                    case ADMIN:
                        uploadedByRole = "Admin Assistance";
                        break;
                    case FINANCE:
                        uploadedByRole = "Finance Coordinator";
                        break;
                    case MARKETING:
                        uploadedByRole = "Marketing Planner";
                        break;
                    case SUPPORT:
                        uploadedByRole = "Client Support Officer";
                        break;
                    case DEVELOPER:
                        uploadedByRole = "Senior Web Developer";
                        break;
                    case DIRECTOR:
                        uploadedByRole = "Managing Director";
                        break;
                    default:
                        uploadedByRole = "Admin";
                        break;
                }
            }
            
            document.setUploadedBy(uploadedByRole); // You can get from session
            document.setUploadedAt(LocalDateTime.now());

            documentRepository.save(document);

            ra.addFlashAttribute("flashMsg", "Document uploaded successfully");
            return "redirect:/admin/documents";

        } catch (IOException e) {
            ra.addFlashAttribute("errorMsg", "Error uploading file: " + e.getMessage());
            return "redirect:/admin/documents/upload";
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            Document document = documentRepository.findById(id).orElse(null);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            // Get the file path
            Path filePath = Paths.get(document.getFilePath());
            
            // Check if file exists
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Create resource from file
            Resource resource = new FileSystemResource(filePath);
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Set headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                      "attachment; filename=\"" + document.getOriginalFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(document.getFileSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            return "redirect:/admin/documents";
        }
        
        model.addAttribute("document", document);
        model.addAttribute("title", "Edit Document · Admin");
        model.addAttribute("pageHeading", "Edit Document");
        model.addAttribute("activeMenu", "documents");
        return "admin/documents/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam("title") String title,
                       @RequestParam("description") String description,
                       @RequestParam("category") String category,
                       @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                       @RequestParam(value = "file", required = false) MultipartFile file,
                       HttpSession session,
                       RedirectAttributes ra) {
        try {
            Document document = documentRepository.findById(id).orElse(null);
            if (document == null) {
                ra.addFlashAttribute("errorMsg", "Document not found");
                return "redirect:/admin/documents";
            }

            // Update basic fields
            document.setTitle(title);
            document.setDescription(description);
            document.setCategory(category);
            document.setIsPublic(isPublic);

            // Handle file update if new file is provided
            if (file != null && !file.isEmpty()) {
                // Delete old file
                Path oldFilePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(oldFilePath);

                // Create upload directory if it doesn't exist
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate new unique filename
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(filename);

                // Save new file
                Files.copy(file.getInputStream(), filePath);

                // Update file-related fields
                document.setFilename(filename);
                document.setFilename2(filename);
                document.setFilePath(filePath.toString());
                document.setOriginalFilename(originalFilename);
                document.setOriginalFilename2(originalFilename);
                document.setFileSize(file.getSize());
                document.setFileType(file.getContentType());
            }

            documentRepository.save(document);

            ra.addFlashAttribute("flashMsg", "Document updated successfully");
            return "redirect:/admin/documents";

        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error updating document: " + e.getMessage());
            return "redirect:/admin/documents/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            Document document = documentRepository.findById(id).orElse(null);
            if (document == null) {
                ra.addFlashAttribute("errorMsg", "Document not found");
                return "redirect:/admin/documents";
            }

            // Delete file from filesystem
            Path filePath = Paths.get(UPLOAD_DIR + document.getFilename());
            Files.deleteIfExists(filePath);

            // Delete from database
            documentRepository.delete(document);

            ra.addFlashAttribute("flashMsg", "Document deleted successfully");
            return "redirect:/admin/documents";

        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error deleting document: " + e.getMessage());
            return "redirect:/admin/documents";
        }
    }
}

