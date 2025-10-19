package com.example.adbridge.repo;

import com.example.adbridge.model.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {
    List<PaymentAuditLog> findByPaymentIdOrderByPerformedAtDesc(Long paymentId);
}




