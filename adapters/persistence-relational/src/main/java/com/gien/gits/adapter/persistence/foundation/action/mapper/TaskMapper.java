package com.gien.gits.adapter.persistence.foundation.action.mapper;

import com.gien.gits.action.domain.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 任务 Mapper — foundation/action 层
 */
@Mapper
public interface TaskMapper {

    void insert(Task task);

    Optional<Task> findByTaskId(@Param("taskId") String taskId);

    List<Task> findByInteractionId(@Param("interactionId") String interactionId);

    List<Task> findByCustomerId(@Param("customerId") String customerId);

    List<Task> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    List<Task> findByStatus(@Param("status") String status);

    List<Task> findByAssignedTo(@Param("assignedTo") String assignedTo);

    List<Task> findOverdue();

    List<Task> findSubTasks(@Param("parentTaskId") String parentTaskId);

    List<Task> findAll();

    void updateStatus(@Param("taskId") String taskId,
                      @Param("status") String status);
}
