package com.example.adbridge.repo;

import com.example.adbridge.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTitleContainingIgnoreCase(String title);
    List<Task> findByStatus(Task.TaskStatus status);
    List<Task> findByStatusOrderByCreatedAtDesc(Task.TaskStatus status);
    List<Task> findByAssignedTo(String assignedTo);
    List<Task> findByPriority(Task.TaskPriority priority);
}