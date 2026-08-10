package com.gien.gits.adapter.persistence.v11;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.action.domain.Task;
import com.gien.gits.action.port.WritableTaskRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC persistence adapter for {@link Task}
 */
public class JdbcTaskRepository implements WritableTaskRepository {

    private static final String INSERT_SQL =
        "INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, " +
        "task_type, title, description, status, priority, assigned_to, assigned_role, " +
        "due_date, completed_date, tags, parent_task_id, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID =
        "SELECT * FROM task WHERE task_id = ?";

    private static final String FIND_BY_INTERACTION =
        "SELECT * FROM task WHERE interaction_id = ? ORDER BY created_at";

    private static final String FIND_BY_CUSTOMER =
        "SELECT * FROM task WHERE customer_id = ? ORDER BY created_at";

    private static final String FIND_BY_CASE =
        "SELECT * FROM task WHERE operating_case_id = ? ORDER BY created_at";

    private static final String FIND_BY_STATUS =
        "SELECT * FROM task WHERE status = ? ORDER BY created_at";

    private static final String FIND_BY_ASSIGNED =
        "SELECT * FROM task WHERE assigned_to = ? ORDER BY created_at";

    private static final String FIND_OVERDUE =
        "SELECT * FROM task WHERE status IN ('TODO','IN_PROGRESS') AND due_date < CURRENT_DATE ORDER BY due_date";

    private static final String FIND_SUB_TASKS =
        "SELECT * FROM task WHERE parent_task_id = ? ORDER BY created_at";

    private static final String UPDATE_STATUS =
        "UPDATE task SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE task_id = ?";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcTaskRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void save(Task t) {
        try {
            jdbc.update(INSERT_SQL,
                t.taskId(), t.interactionId(), t.customerId(), t.operatingCaseId(),
                t.taskType(), t.title(), t.description(), t.status(), t.priority(),
                t.assignedTo(), t.assignedRole(), t.dueDate(), t.completedDate(),
                objectMapper.writeValueAsString(t.tags()), t.parentTaskId(),
                Timestamp.from(t.createdAt()), Timestamp.from(t.updatedAt()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Task", e);
        }
    }

    @Override
    public void updateStatus(String taskId, String status) {
        jdbc.update(UPDATE_STATUS, status, taskId);
    }

    @Override
    public Optional<Task> findByTaskId(String taskId) {
        List<Task> results = jdbc.query(FIND_BY_ID, new TaskRowMapper(objectMapper), taskId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Task> findByInteractionId(String interactionId) {
        return jdbc.query(FIND_BY_INTERACTION, new TaskRowMapper(objectMapper), interactionId);
    }

    @Override
    public List<Task> findByCustomerId(String customerId) {
        return jdbc.query(FIND_BY_CUSTOMER, new TaskRowMapper(objectMapper), customerId);
    }

    @Override
    public List<Task> findByOperatingCaseId(String operatingCaseId) {
        return jdbc.query(FIND_BY_CASE, new TaskRowMapper(objectMapper), operatingCaseId);
    }

    @Override
    public List<Task> findByStatus(String status) {
        return jdbc.query(FIND_BY_STATUS, new TaskRowMapper(objectMapper), status);
    }

    @Override
    public List<Task> findByAssignedTo(String assignedTo) {
        return jdbc.query(FIND_BY_ASSIGNED, new TaskRowMapper(objectMapper), assignedTo);
    }

    @Override
    public List<Task> findOverdue() {
        return jdbc.query(FIND_OVERDUE, new TaskRowMapper(objectMapper));
    }

    @Override
    public List<Task> findSubTasks(String parentTaskId) {
        return jdbc.query(FIND_SUB_TASKS, new TaskRowMapper(objectMapper), parentTaskId);
    }

    @Override
    public List<Task> findAll() {
        return jdbc.query("SELECT * FROM task ORDER BY created_at DESC", new TaskRowMapper(objectMapper));
    }

    private static List<String> parseJsonStringList(ObjectMapper om, String json) {
        try {
            return om.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Fallback: try unquoting if H2 wrapped it as a string literal
            String trimmed = json.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                try {
                    String unquoted = om.readValue(trimmed, String.class);
                    return om.readValue(unquoted, new TypeReference<List<String>>() {});
                } catch (Exception ignored) {}
            }
            return List.of();
        }
    }

    private static final class TaskRowMapper implements RowMapper<Task> {
        private final ObjectMapper objectMapper;

        TaskRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String tagsJson = rs.getString("tags");
                List<String> tags = tagsJson != null
                    ? parseJsonStringList(objectMapper, tagsJson) : List.of();
                return new Task(
                    rs.getString("task_id"), rs.getString("interaction_id"),
                    rs.getString("customer_id"), rs.getString("operating_case_id"),
                    rs.getString("task_type"), rs.getString("title"),
                    rs.getString("description"), rs.getString("status"),
                    rs.getString("priority"), rs.getString("assigned_to"),
                    rs.getString("assigned_role"), rs.getString("due_date"),
                    rs.getString("completed_date"), tags, rs.getString("parent_task_id"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant());
            } catch (Exception e) {
                throw new SQLException("Failed to map Task", e);
            }
        }
    }
}
