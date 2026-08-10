package com.gien.gits.action.port;

import com.gien.gits.action.domain.Task;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储端口
 */
public interface TaskRepository {
    Optional<Task> findByTaskId(String taskId);
    List<Task> findByInteractionId(String interactionId);
    List<Task> findByCustomerId(String customerId);
    List<Task> findByOperatingCaseId(String operatingCaseId);
    List<Task> findByStatus(String status);
    List<Task> findByAssignedTo(String assignedTo);
    List<Task> findOverdue();
    List<Task> findSubTasks(String parentTaskId);
    List<Task> findAll();
}
