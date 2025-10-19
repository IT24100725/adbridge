package com.example.adbridge.repo;

import com.example.adbridge.model.Notification;
import com.example.adbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific user, ordered by creation date (newest first)
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a specific user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Count unread notifications for a specific user
    long countByUserAndIsReadFalse(User user);
    
    // Find notifications for a user with pagination
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findUserNotifications(User user);
}











