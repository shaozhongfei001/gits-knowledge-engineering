-- ═══════════════════════════════════════════════════════════════════
-- V014: V1.1 种子数据 — 华东精工经营闭环场景
-- 包含: operating_case, commitment, task, opportunity
-- ═══════════════════════════════════════════════════════════════════

-- ── 经营案例 ──
INSERT INTO operating_case (case_id, case_type, status, purpose, valid_from, valid_to, recorded_at, record_version, created_by)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'CONTINUOUS_ENGAGEMENT', 'ACTIVE', '华东精工装备集团持续经营闭环 — 智能制造二期项目跟进',
        '2026-07-08 09:00:00', NULL, '2026-07-08 09:00:00', 0, 'RM-ZW-001');

-- ── 承诺 ──
INSERT INTO commitment (commitment_id, operating_case_id, journey_id, commitment_type, content, owner, due_date, status, evidence_ref, created_at, fulfilled_at, updated_at)
VALUES ('b1b2c3d4-e5f6-7890-abcd-ef0123456780', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', NULL, 'CUSTOMER_COMMITMENT', '提供设备清单和付款节奏', 'C-FIN-001', '2026-07-10', 'OPEN', 'NOTE-008',
        '2026-07-08 10:30:00', NULL, '2026-07-08 10:30:00');

INSERT INTO commitment (commitment_id, operating_case_id, journey_id, commitment_type, content, owner, due_date, status, evidence_ref, created_at, fulfilled_at, updated_at)
VALUES ('b1b2c3d4-e5f6-7890-abcd-ef0123456781', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', NULL, 'BANK_COMMITMENT', '提供三年期融资结构建议', 'P-RM-001', '2026-07-14', 'OPEN', 'NOTE-008',
        '2026-07-08 10:30:00', NULL, '2026-07-08 10:30:00');

INSERT INTO commitment (commitment_id, operating_case_id, journey_id, commitment_type, content, owner, due_date, status, evidence_ref, created_at, fulfilled_at, updated_at)
VALUES ('b1b2c3d4-e5f6-7890-abcd-ef0123456782', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', NULL, 'BANK_COMMITMENT', '提供供应商付款工具对照', 'P-TB-001', '2026-07-14', 'OPEN', 'NOTE-008',
        '2026-07-08 10:30:00', NULL, '2026-07-08 10:30:00');

-- ── 任务 ──
INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, task_type, title, description, status, priority, assigned_to, assigned_role, due_date, completed_date, tags, parent_task_id, created_at, updated_at)
VALUES ('c1b2c3d4-e5f6-7890-abcd-ef0123456780', NULL, 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'FOLLOW_UP', '跟进设备清单', '跟进华东精工提供设备清单和付款节奏', 'OPEN', 'HIGH', 'P-RM-001', 'RELATIONSHIP_MANAGER', '2026-07-10', NULL, '["跟进","设备清单"]', NULL, '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, task_type, title, description, status, priority, assigned_to, assigned_role, due_date, completed_date, tags, parent_task_id, created_at, updated_at)
VALUES ('c1b2c3d4-e5f6-7890-abcd-ef0123456781', NULL, 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'BANK_DELIVERABLE', '形成三年期融资结构建议', '为华东精工制定三年期融资结构建议方案', 'OPEN', 'HIGH', 'P-RM-001', 'RELATIONSHIP_MANAGER', '2026-07-14', NULL, '["融资","方案"]', NULL, '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, task_type, title, description, status, priority, assigned_to, assigned_role, due_date, completed_date, tags, parent_task_id, created_at, updated_at)
VALUES ('c1b2c3d4-e5f6-7890-abcd-ef0123456782', NULL, 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'SPECIALIST_COLLAB', '供应商付款工具对照', '整理供应商付款工具对照表', 'OPEN', 'MEDIUM', 'P-TB-001', 'PRODUCT_SPECIALIST', '2026-07-14', NULL, '["供应商","付款工具"]', NULL, '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, task_type, title, description, status, priority, assigned_to, assigned_role, due_date, completed_date, tags, parent_task_id, created_at, updated_at)
VALUES ('c1b2c3d4-e5f6-7890-abcd-ef0123456783', NULL, 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'SPECIALIST_COLLAB', '评估两条路径的Preliminary Bankability边界', '评估集团本部和项目公司两条融资路径的初步可行边界', 'OPEN', 'HIGH', 'P-RISK-001', 'COMPLIANCE_OFFICER', '2026-07-14', NULL, '["风险评估","Bankability"]', NULL, '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO task (task_id, interaction_id, customer_id, operating_case_id, task_type, title, description, status, priority, assigned_to, assigned_role, due_date, completed_date, tags, parent_task_id, created_at, updated_at)
VALUES ('c1b2c3d4-e5f6-7890-abcd-ef0123456784', NULL, 'CUST-CORP-0001', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'SPECIALIST_COLLAB', '确认设备更新贷款V2.2适用条件', '确认设备更新贷款V2.2产品的适用条件和材料缺口', 'OPEN', 'MEDIUM', 'P-PM-001', 'PRODUCT_SPECIALIST', '2026-07-14', NULL, '["产品","设备更新贷款"]', NULL, '2026-07-08 10:30:00', '2026-07-08 10:30:00');

-- ── 商机 ──
INSERT INTO opportunity (opportunity_id, customer_id, interaction_id, operating_case_id, opportunity_type, product_id, product_name, description, status, estimated_amount, probability, assigned_to, source, next_steps, expected_close_date, created_at, updated_at)
VALUES ('d1b2c3d4-e5f6-7890-abcd-ef0123456780', 'CUST-CORP-0001', NULL, 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'PROJECT_FINANCING', 'PROD-PROJECT-LOAN', '项目贷款', '智能制造二期项目融资 — 总投资1.2亿元', 'QUALIFIED', '12000万', '60%', 'P-RM-001', 'EXTERNAL_EVENT', '["确认项目投资额","准备项目备案文件","安排客户面谈"]', '2026-09-30', '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO opportunity (opportunity_id, customer_id, interaction_id, operating_case_id, opportunity_type, product_id, product_name, description, status, estimated_amount, probability, assigned_to, source, next_steps, expected_close_date, created_at, updated_at)
VALUES ('d1b2c3d4-e5f6-7890-abcd-ef0123456781', 'CUST-CORP-0001', NULL, 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'EQUIPMENT_FINANCING', 'PROD-BANK-ACCEPTANCE', '银行承兑汇票', '设备采购支付工具 — 西门子数控系统等进口设备', 'DETECTED', '3000万', '40%', 'P-RM-001', 'INTERACTION', '["确认设备采购清单","评估承兑汇票额度需求"]', '2026-08-31', '2026-07-08 10:30:00', '2026-07-08 10:30:00');

INSERT INTO opportunity (opportunity_id, customer_id, interaction_id, operating_case_id, opportunity_type, product_id, product_name, description, status, estimated_amount, probability, assigned_to, source, next_steps, expected_close_date, created_at, updated_at)
VALUES ('d1b2c3d4-e5f6-7890-abcd-ef0123456782', 'CUST-CORP-0001', NULL, 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'WORKING_CAPITAL', 'PROD-WORKING-CAPITAL', '流动资金贷款', '日常经营周转资金增额 — 设备付款激增', 'PROPOSAL', '2500万', '70%', 'P-RM-001', 'TRANSACTION_ANALYSIS', '["确认增额需求","准备增额审批材料"]', '2026-08-15', '2026-07-08 10:30:00', '2026-07-08 10:30:00');
