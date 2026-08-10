package com.gien.gits.api.service;

import com.gien.gits.action.domain.Task;
import com.gien.gits.action.port.WritableTaskRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 任务服务 — 跟进任务管理
 */
public class TaskService {

    private final WritableTaskRepository taskRepo;

    public TaskService(WritableTaskRepository taskRepo) {
        this.taskRepo = Objects.requireNonNull(taskRepo);
    }

    public Optional<Task> findById(String taskId) {
        return taskRepo.findByTaskId(taskId);
    }

    public List<Task> findByInteractionId(String interactionId) {
        return taskRepo.findByInteractionId(interactionId);
    }

    public List<Task> findByCustomerId(String customerId) {
        return taskRepo.findByCustomerId(customerId);
    }

    public List<Task> findByOperatingCaseId(String operatingCaseId) {
        return taskRepo.findByOperatingCaseId(operatingCaseId);
    }

    public List<Task> findByStatus(String status) {
        return taskRepo.findByStatus(status);
    }

    public List<Task> findByAssignedTo(String assignedTo) {
        return taskRepo.findByAssignedTo(assignedTo);
    }

    public List<Task> findOverdue() {
        return taskRepo.findOverdue();
    }

    public List<Task> findAll() {
        return taskRepo.findAll();
    }

    public Task create(Task task) {
        taskRepo.save(task);
        return task;
    }

    public Optional<Task> updateStatus(String taskId, String status) {
        taskRepo.updateStatus(taskId, status);
        return taskRepo.findByTaskId(taskId);
    }
}
