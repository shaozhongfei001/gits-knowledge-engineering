-- ═══════════════════════════════════════════════════════════════════
-- V017: V1.1 种子数据 — Opportunity Signal / KYC Gap Profile
-- MySQL version
-- ═══════════════════════════════════════════════════════════════════

-- ── 机会信号 (3条) ──

-- 1. DETECTED — 融资需求信号
INSERT INTO opportunity_signal (signal_id, operating_case_id, journey_id, signal_type, content, source_type, source_ref, confidence, status, evidence_ref, detected_at, confirmed_at, created_at, updated_at)
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'jny-001', 'FINANCING_NEED',
        '客户提及智能制造二期项目，预计投资3000万，有设备更新融资需求',
        'INTERACTION', 'TR-001', 0.85, 'DETECTED', 'evidence-001',
        '2026-08-12T10:00:00Z', NULL, '2026-08-12T10:00:00Z', NULL);

-- 2. CONFIRMED — 产品机会信号
INSERT INTO opportunity_signal (signal_id, operating_case_id, journey_id, signal_type, content, source_type, source_ref, confidence, status, evidence_ref, detected_at, confirmed_at, created_at, updated_at)
VALUES ('7c9e6679-7425-40de-944b-e07fc1f90ae7', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'jny-001', 'PRODUCT_OPPORTUNITY',
        '客户对供应链金融产品表示兴趣，可推荐供应商付款工具',
        'ANALYSIS', 'kyc-report-001', 0.72, 'CONFIRMED', 'kyc-report-001',
        '2026-08-12T10:30:00Z', '2026-08-12T14:00:00Z', '2026-08-12T10:30:00Z', '2026-08-12T14:00:00Z');

-- 3. DISMISSED — 关系变化信号（已排除）
INSERT INTO opportunity_signal (signal_id, operating_case_id, journey_id, signal_type, content, source_type, source_ref, confidence, status, evidence_ref, detected_at, confirmed_at, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'a1b2c3d4-e5f6-7890-abcd-ef0123456789', 'jny-001', 'RELATIONSHIP_CHANGE',
        '检测到客户实控人变更信号，需核实',
        'EXTERNAL_EVENT', 'event-registry-001', 0.45, 'DISMISSED', 'event-registry-001',
        '2026-08-12T09:00:00Z', '2026-08-12T11:00:00Z', '2026-08-12T09:00:00Z', '2026-08-12T11:00:00Z');

-- ── KYC缺口画像 (2条) ──

-- 1. 华东精工 — 中等完备度
INSERT INTO kyc_gap_profile (profile_id, customer_id, as_of, known_items, partial_known_items, stale_items, conflicting_or_ambiguous_items, unknown_items, priority_questions, created_at, updated_at)
VALUES ('kgp-0001-huadong', 'CUST-CORP-0001', '2026-08-12',
        '["公司基本信息","主营业务","实控人信息","信用评级"]',
        '["集团担保能力","关联企业清单"]',
        '["财务数据(2024年报)"]',
        '["实控人持股比例 — 工商登记与客户表述不一致"]',
        '["海外子公司经营状况","供应链上游依赖度","ESG合规状态"]',
        '["核实集团担保范围","更新财务数据至2025年","确认海外子公司是否纳入授信主体"]',
        '2026-08-12T10:30:00Z', '2026-08-12T10:30:00Z');

-- 2. 华南贸易 — 低完备度
INSERT INTO kyc_gap_profile (profile_id, customer_id, as_of, known_items, partial_known_items, stale_items, conflicting_or_ambiguous_items, unknown_items, priority_questions, created_at, updated_at)
VALUES ('kgp-0002-huanan', 'CUST-CORP-0002', '2026-08-12',
        '["公司基本信息","主营业务"]',
        '["实控人信息"]',
        '["信用评级(2023)"]',
        '[]',
        '["财务数据","关联企业清单","担保能力评估","供应链关系","ESG合规状态"]',
        '["补充实控人背景调查","获取最新财务报表","重新评估信用等级"]',
        '2026-08-12T11:00:00Z', '2026-08-12T11:00:00Z');
