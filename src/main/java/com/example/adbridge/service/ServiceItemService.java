//package com.example.adbridge.service;
//
//import com.example.adbridge.model.ServiceItem;
//import com.example.adbridge.repo.ServiceItemRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class ServiceItemService {
//
//    private final ServiceItemRepository repo;
//
//    public ServiceItemService(ServiceItemRepository repo) {
//        this.repo = repo;
//    }
//
//    public List<ServiceItem> findAll() {
//        return repo.findAll();
//    }
//
//    public ServiceItem get(Long id) {
//        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
//    }
//
//    @Transactional
//    public ServiceItem save(ServiceItem s) {
//        // If user gives a slug and it collides, append increment
//        if (s.getSlug() != null && !s.getSlug().isBlank()) {
//            String base = s.getSlug();
//            String candidate = base;
//            int i = 1;
//            while (repo.existsBySlug(candidate) &&
//                    (s.getId() == null || repo.findBySlug(candidate).map(e -> !e.getId().equals(s.getId())).orElse(true))) {
//                candidate = base + "-" + i++;
//            }
//            s.setSlug(candidate);
//        }
//        return repo.save(s);
//    }
//
//    @Transactional
//    public void delete(Long id) {
//        repo.deleteById(id);
//    }
//}