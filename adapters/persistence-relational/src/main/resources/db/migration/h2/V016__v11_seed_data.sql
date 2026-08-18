-- ═══════════════════════════════════════════════════════════════════
-- V016: V1.1 种子数据 — Human Gate / CRM Writeback / Audit Trace
-- H2-compatible version (MySQL compatibility mode)
-- ═══════════════════════════════════════════════════════════════════

-- ── 人工门禁 (5条) ──

-- 1. PENDING — A01_OUTREACH 触达审批
INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id, status, subject, proposal, evidence_refs, decision, modification, decision_reason, actor_id, created_at, decided_at)
VALUES ('hg-0001-a01-outreach', 'A01_OUTREACH', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'PENDING', '华东精工触达方案审批',
        '{"channel":"VISIT","purpose":"智能制造二期项目跟进","contactPerson":"张总","scheduledDate":"2026-08-15"}',
        '["evidence-001","evidence-002"]',
        NULL, NULL, NULL, NULL,
        '2026-08-12T09:00:00Z', NULL);

-- 2. PENDING — B01_CONTEXT_ENRICH 上下文丰富审批
INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id, status, subject, proposal, evidence_refs, decision, modification, decision_reason, actor_id, created_at, decided_at)
VALUES ('hg-0002-b01-kyc', 'B01_CONTEXT_ENRICH', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'PENDING', 'KYC洞察报告审批 — 集团本部路径',
        '{"riskLevel":"MEDIUM","findings":["实控人信用良好","集团担保能力充足"],"recommendation":"建议推进集团本部融资路径"}',
        '["kyc-report-001","credit-report-001"]',
        NULL, NULL, NULL, NULL,
        '2026-08-12T10:30:00Z', NULL);

-- 3. APPROVED — C01_PREVISIT_APPROVE 访前审批
INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id, status, subject, proposal, evidence_refs, decision, modification, decision_reason, actor_id, created_at, decided_at)
VALUES ('hg-0003-c01-plan', 'C01_PREVISIT_APPROVE', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'APPROVED', '经营计划审批 — 智能制造二期',
        '{"planType":"CONTINUOUS_ENGAGEMENT","objectives":["设备更新贷款","项目融资","供应商付款工具"],"timeline":"Q3-Q4 2026"}',
        '["plan-doc-001","budget-001"]',
        'APPROVE', NULL, '计划合理，覆盖多产品线', 'P-RM-001',
        '2026-08-12T11:00:00Z', '2026-08-12T14:30:00Z');

-- 4. PENDING — F01_CRM_WRITEBACK CRM写回审批
INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id, status, subject, proposal, evidence_refs, decision, modification, decision_reason, actor_id, created_at, decided_at)
VALUES ('hg-0004-f01-crm', 'F01_CRM_WRITEBACK', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'PENDING', 'CRM写回审批 — 客户经营状态更新',
        '{"targetEntity":"CustomerProfile","operation":"UPDATE","fields":{"operatingStatus":"EXPANDING","lastEngagementDate":"2026-08-12"}}',
        '["crm-snapshot-001"]',
        NULL, NULL, NULL, NULL,
        '2026-08-12T15:00:00Z', NULL);

-- 5. REJECTED — D01_PRODUCT_RECOMMEND 产品推荐审批
INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id, status, subject, proposal, evidence_refs, decision, modification, decision_reason, actor_id, created_at, decided_at)
VALUES ('hg-0005-d01-evidence', 'D01_PRODUCT_RECOMMEND', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'REJECTED', '证据包审批 — 设备清单完整性',
        '{"evidenceType":"EQUIPMENT_LIST","description":"智能制造二期设备采购清单","completeness":"PARTIAL"}',
        '["equip-list-v1","quote-siemens-001"]',
        'REJECT', NULL, '证据不完整，缺少国产设备报价', 'P-RISK-001',
        '2026-08-12T16:00:00Z', '2026-08-12T17:00:00Z');

-- ── CRM写回命令 (3条) ──

-- 1. PENDING — 待审批
INSERT INTO crm_writeback_command (command_id, journey_id, customer_id, operating_case_id, operation, target_entity, payload, status, human_confirmation_required, decision, modifications, decision_reason, actor_id, created_at, decided_at, sent_at, error_message)
VALUES ('crm-cmd-0001', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'UPDATE', 'CustomerProfile',
        '{"operatingStatus":"EXPANDING","lastEngagementDate":"2026-08-12","engagementScore":85}',
        'PENDING', TRUE, NULL, NULL, NULL, NULL,
        '2026-08-12T15:00:00Z', NULL, NULL, NULL);

-- 2. APPROVED — 已审批待发送
INSERT INTO crm_writeback_command (command_id, journey_id, customer_id, operating_case_id, operation, target_entity, payload, status, human_confirmation_required, decision, modifications, decision_reason, actor_id, created_at, decided_at, sent_at, error_message)
VALUES ('crm-cmd-0002', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'UPDATE', 'OpportunityRecord',
        '{"opportunityId":"d1b2c3d4-e5f6-7890-abcd-ef0123456780","status":"QUALIFIED","probability":"60%"}',
        'APPROVED', TRUE, 'APPROVE', NULL, '商机状态更新确认', 'P-RM-001',
        '2026-08-12T14:00:00Z', '2026-08-12T14:30:00Z', NULL, NULL);

-- 3. SENT — 已发送
INSERT INTO crm_writeback_command (command_id, journey_id, customer_id, operating_case_id, operation, target_entity, payload, status, human_confirmation_required, decision, modifications, decision_reason, actor_id, created_at, decided_at, sent_at, error_message)
VALUES ('crm-cmd-0003', 'jny-001', 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'CREATE', 'InteractionRecord',
        '{"type":"VISIT","date":"2026-08-12","summary":"华东精工拜访 — 智能制造二期项目对接","participants":["P-RM-001","张总"]}',
        'SENT', TRUE, 'APPROVE', NULL, '交互记录创建确认', 'P-RM-001',
        '2026-08-12T09:00:00Z', '2026-08-12T09:15:00Z', '2026-08-12T09:20:00Z', NULL);

-- ── 审计追踪 (5条) ──

-- 1. HumanGate DECIDE 审计
INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation, before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
VALUES ('at-0001', 'HumanGate', 'hg-0003-c01-plan', 'DECIDE',
        '{"status":"PENDING","gateType":"C01_PREVISIT_APPROVE"}',
        '{"status":"APPROVED","decision":"APPROVE","actorId":"P-RM-001"}',
        'P-RM-001', 'RELATIONSHIP_MANAGER', '2026-08-12T14:30:00Z', 'corr-jny-001');

-- 2. HumanGate REJECT 审计
INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation, before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
VALUES ('at-0002', 'HumanGate', 'hg-0005-d01-evidence', 'DECIDE',
        '{"status":"PENDING","gateType":"D01_PRODUCT_RECOMMEND"}',
        '{"status":"REJECTED","decision":"REJECT","actorId":"P-RISK-001"}',
        'P-RISK-001', 'COMPLIANCE_OFFICER', '2026-08-12T17:00:00Z', 'corr-jny-001');

-- 3. CrmWritebackCommand DECIDE 审计
INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation, before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
VALUES ('at-0003', 'CrmWritebackCommand', 'crm-cmd-0002', 'DECIDE',
        '{"status":"PENDING","operation":"UPDATE","targetEntity":"OpportunityRecord"}',
        '{"status":"APPROVED","decision":"APPROVE","actorId":"P-RM-001"}',
        'P-RM-001', 'RELATIONSHIP_MANAGER', '2026-08-12T14:30:00Z', 'corr-jny-001');

-- 4. CrmWritebackCommand SEND 审计
INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation, before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
VALUES ('at-0004', 'CrmWritebackCommand', 'crm-cmd-0003', 'SEND',
        '{"status":"APPROVED"}',
        '{"status":"SENT","sentAt":"2026-08-12T09:20:00Z"}',
        'SYSTEM', 'SYSTEM', '2026-08-12T09:20:00Z', 'corr-jny-001');

-- 5. Claim RECORD 审计
INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation, before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
VALUES ('at-0005', 'Claim', 'claim-001-kyc', 'RECORD',
        NULL,
        '{"claimType":"KYC_INSIGHT","customerId":"CUST-CORP-0001","status":"CANDIDATE"}',
        'SYSTEM', 'KYC_ENGINE', '2026-08-12T10:30:00Z', 'corr-jny-001');
