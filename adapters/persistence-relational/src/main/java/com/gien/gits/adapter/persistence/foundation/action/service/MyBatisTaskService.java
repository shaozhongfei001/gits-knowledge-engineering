package com.gien.gits.adapter.persistence.foundation.action.service;

import com.gien.gits.action.domain.Task;
import com.gien.gits.action.port.TaskRepository;
import com.gien.gits.action.port.WritableTaskRepository;
import com.gien.gits.adapter.persistence.foundation.action.mapper.TaskMapper;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 实现 — 任务仓储
 */
public class MyBatisTaskService implements TaskRepository, WritableTaskRepository {

    private final TaskMapper mapper;

    public MyBatisTaskService(TaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Task> findByTaskId(String taskId) {
        return mapper.findByTaskId(taskId);
    }

    @Override
    public List<Task> findByInteractionId(String interactionId) {
        return mapper.findByInteractionId(interactionId);
    }

    @Override
    public List<Task> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<Task> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }

    @Override
    public List<Task> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public List<Task> findByAssignedTo(String assignedTo) {
        return mapper.findByAssignedTo(assignedTo);
    }

    @Override
    public List<Task> findOverdue() {
        return mapper.findOverdue();
    }

    @Override
    public List<Task> findSubTasks(String parentTaskId) {
        return mapper.findSubTasks(parentTaskId);
    }

    @Override
    public List<Task> findAll() {
        return mapper.findAll();
    }

    @Override
    public void save(Task task) {
        mapper.insert(task);
    }

    @Override
    public void updateStatus(String taskId, String status) {
        mapper.updateStatus(taskId, status);
    }
}
