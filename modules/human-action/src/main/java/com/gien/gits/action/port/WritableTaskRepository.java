package com.gien.gits.action.port;

import com.gien.gits.action.domain.Task;

/**
 * 任务可写仓储端口
 */
public interface WritableTaskRepository extends TaskRepository {
    void save(Task task);
    void updateStatus(String taskId, String status);
}
