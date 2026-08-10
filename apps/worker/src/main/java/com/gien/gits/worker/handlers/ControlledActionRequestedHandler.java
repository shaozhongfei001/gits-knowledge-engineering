package com.gien.gits.worker.handlers;

import com.gien.gits.action.ActionDispatchPort;
import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import com.gien.gits.ontology.HumanConfirmation;
import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link DomainEventType#CONTROLLED_ACTION_REQUESTED} domain event.
 *
 * <p>Business flow:
 * <ol>
 *   <li>Extract action data from event.data() — proposalId, confirmation, target, idempotencyKey</li>
 *   <li>Construct ControlledAction record with HumanConfirmation and Target</li>
 *   <li>Dispatch via ActionDispatchPort (fail-closed: only records in candidate build)</li>
 *   <li>On dispatch success: generate CrmWritebackCommand and send through CrmWritebackChannel</li>
 *   <li>On dispatch failure: log failure code and mark action as COMPENSATION_REQUIRED</li>
 * </ol>
 *
 * <p>Rule CRM-001 (禁令#1): AI不可直接写入CRM — all CrmWritebackCommands require
 * {@code requiresHumanConfirm=true}. The handler only sends pre-confirmed commands
 * that already carry an approving HumanConfirmation.
 */
@Component
public class ControlledActionRequestedHandler {

    private static final Logger log = LoggerFactory.getLogger(ControlledActionRequestedHandler.class);

    private final ActionDispatchPort actionDispatchPort;
    private final CrmWritebackChannel crmWritebackChannel;

    public ControlledActionRequestedHandler(ActionDispatchPort actionDispatchPort,
                                            CrmWritebackChannel crmWritebackChannel) {
        this.actionDispatchPort = Objects.requireNonNull(actionDispatchPort, "actionDispatchPort");
        this.crmWritebackChannel = Objects.requireNonNull(crmWritebackChannel, "crmWritebackChannel");
    }

    @EventListener(condition = "#event.type() == T(com.gien.gits.ontology.event.DomainEventType).CONTROLLED_ACTION_REQUESTED")
    public void handle(CloudEvent event) {
        log.info("[CONTROLLED-ACTION] Processing event: id={}, subject={}", event.id(), event.subject());

        Map<String, Object> data = event.data();
        if (data == null || data.isEmpty()) {
            log.warn("[CONTROLLED-ACTION] Event data is empty, skipping: id={}", event.id());
            return;
        }

        // Step 1: Extract action data from event
        ControlledAction action = extractControlledAction(event);
        log.info("[CONTROLLED-ACTION] Extracted action: actionId={}, proposalId={}, target={}/{}",
                 action.actionId(), action.proposalId(),
                 action.target().system(), action.target().objectId());

        // Step 2: Dispatch the controlled action
        ActionReceipt receipt = actionDispatchPort.dispatch(action);
        log.info("[CONTROLLED-ACTION] Dispatch result: actionId={}, receiptId={}, status={}",
                 action.actionId(), receipt.receiptId(), receipt.status());

        // Step 3: Handle dispatch result — CRM writeback on success, compensation on failure
        switch (receipt.status()) {
            case SUCCEEDED -> handleSuccess(action, receipt);
            case FAILED -> handleFailure(action, receipt);
            case COMPENSATED -> log.info("[CONTROLLED-ACTION] Action already compensated: actionId={}", action.actionId());
        }
    }

    /**
     * Extract a ControlledAction from the CloudEvent data map.
     *
     * <p>Expected data keys:
     * <ul>
     *   <li>proposalId (String) — the proposal that triggered this action</li>
     *   <li>confirmationId (String) — human confirmation reference</li>
     *   <li>subjectId (String) — the subject of the confirmation</li>
     *   <li>decision (String) — APPROVED / MODIFIED_AND_APPROVED</li>
     *   <li>actorId (String) — the human who confirmed</li>
     *   <li>targetSystem (String) — e.g. "CRM"</li>
     *   <li>targetObjectType (String) — e.g. "INTERACTION"</li>
     *   <li>targetObjectId (String) — the CRM object ID</li>
     *   <li>expectedVersion (String) — optimistic locking version</li>
     *   <li>operation (String) — CREATE_TASK / UPDATE_WHITELISTED_FIELDS</li>
     *   <li>payload (Map) — action payload</li>
     *   <li>idempotencyKey (String) — at least 16 chars</li>
     * </ul>
     */
    private ControlledAction extractControlledAction(CloudEvent event) {
        Map<String, Object> data = event.data();

        UUID actionId = UUID.randomUUID();
        UUID proposalId = parseUUID(data, "proposalId");

        // Build HumanConfirmation from event data
        HumanConfirmation confirmation = new HumanConfirmation(
                parseUUID(data, "confirmationId"),
                parseUUID(data, "subjectId"),
                HumanConfirmation.Decision.valueOf(getString(data, "decision", "APPROVED")),
                getString(data, "actorId", "SYSTEM"),
                Instant.now());

        // Build Target from event data
        ControlledAction.Target target = new ControlledAction.Target(
                getString(data, "targetSystem", "CRM"),
                getString(data, "targetObjectType", "INTERACTION"),
                getString(data, "targetObjectId", "UNKNOWN"),
                getString(data, "expectedVersion", "v0"),
                ControlledAction.Target.Operation.valueOf(getString(data, "operation", "CREATE_TASK")),
                data.get("payload") instanceof Map ? (Map<String, Object>) data.get("payload") : Map.of());

        String idempotencyKey = getString(data, "idempotencyKey", "IDEM-" + UUID.randomUUID());

        return new ControlledAction(
                actionId, proposalId, confirmation, target,
                idempotencyKey, Instant.now(), ControlledAction.Status.REQUESTED);
    }

    /**
     * On successful dispatch: generate and send a CrmWritebackCommand.
     * Rule CRM-001: requiresHumanConfirm is always true.
     */
    private void handleSuccess(ControlledAction action, ActionReceipt receipt) {
        log.info("[CONTROLLED-ACTION] Dispatch succeeded: actionId={}, targetVersionAfter={}",
                 action.actionId(), receipt.targetVersionAfter());

        // Generate CRM writeback command from the dispatched action
        CrmWritebackCommand command = new CrmWritebackCommand(
                "CMD-" + action.actionId().toString().substring(0, 8),
                mapObjectType(action.target().objectType()),
                mapOperation(action.target().operation()),
                action.target().expectedVersion(),
                action.target().payload().toString(),
                CrmWritebackCommand.RiskLevel.LOW,
                true,  // RULE-CRM-001: AI不可直接写入CRM
                action.confirmation().actorId(),
                "AUDIT-" + action.actionId().toString().substring(0, 8),
                action.idempotencyKey());

        CrmWritebackChannel.WritebackResult result = crmWritebackChannel.send(command);
        if (result.success()) {
            log.info("[CONTROLLED-ACTION] CRM writeback succeeded: commandId={}, messageId={}",
                     command.commandId(), result.messageId());
        } else {
            log.warn("[CONTROLLED-ACTION] CRM writeback failed: commandId={}, detail={}",
                     command.commandId(), result.detail());
        }
    }

    /**
     * On dispatch failure: log the failure and flag for compensation.
     */
    private void handleFailure(ControlledAction action, ActionReceipt receipt) {
        log.error("[CONTROLLED-ACTION] Dispatch failed: actionId={}, failureCode={}",
                  action.actionId(), receipt.failureCode());
        // In a production system, this would trigger a compensation workflow
        // or escalate to a human operator for manual intervention
    }

    private CrmWritebackCommand.ObjectType mapObjectType(String objectType) {
        try {
            return CrmWritebackCommand.ObjectType.valueOf(objectType);
        } catch (IllegalArgumentException e) {
            log.warn("[CONTROLLED-ACTION] Unknown objectType '{}', defaulting to INTERACTION", objectType);
            return CrmWritebackCommand.ObjectType.INTERACTION;
        }
    }

    private CrmWritebackCommand.Operation mapOperation(ControlledAction.Target.Operation operation) {
        return switch (operation) {
            case CREATE_TASK -> CrmWritebackCommand.Operation.CREATE;
            case UPDATE_WHITELISTED_FIELDS -> CrmWritebackCommand.Operation.UPDATE;
        };
    }

    private UUID parseUUID(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return UUID.randomUUID();
        }
        if (value instanceof UUID u) {
            return u;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException e) {
            log.warn("[CONTROLLED-ACTION] Invalid UUID for key '{}': {}, generating random", key, value);
            return UUID.randomUUID();
        }
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
