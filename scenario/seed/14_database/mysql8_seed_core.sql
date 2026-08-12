USE rm_continuous_demo;

INSERT INTO legal_entity(entity_id,name,short_name,entity_type,role_desc,ownership_parent,ownership_pct,bank_customer_id,status) VALUES
('ENT-001','华东精工装备集团有限公司','华东精工','LEGAL_ENTITY','集团本部/当前授信主体',NULL,NULL,'CUST-001','ACTIVE'),
('ENT-002','华东精工智能制造有限公司','智能制造公司','LEGAL_ENTITY','二期项目潜在项目主体','ENT-001',100,'CUST-002','ACTIVE'),
('ENT-003','华东精工自动化设备有限公司','自动化公司','LEGAL_ENTITY','生产/设备采购主体之一','ENT-001',80,'CUST-003','ACTIVE'),
('ENT-004','华东精工进出口有限公司','进出口公司','LEGAL_ENTITY','进出口及跨境结算主体','ENT-001',100,'CUST-004','ACTIVE');

INSERT INTO credit_facility(facility_id,borrower_entity_id,facility_type,approved_amount_cny,used_amount_cny,available_amount_cny,approval_date,expiry_date,allowed_purpose,restriction_desc,status) VALUES
('FAC-001','ENT-001','综合授信',150000000,110000000,40000000,'2026-01-15','2027-01-14',
'日常经营周转、原材料采购、短期订单资金周转',
'固定资产投资需另行核实用途和审批；项目主体变化需重新判断结构',
'ACTIVE');

INSERT INTO commitment(commitment_id,side,owner_id,content,due_date,status,source_ref) VALUES
('COM-CUST-001','CUSTOMER','C-FIN-001','提供设备清单和付款节奏','2026-07-10','OPEN','NOTE-008'),
('COM-BANK-001','BANK','P-RM-001','提供三年期融资结构建议','2026-07-14','OPEN','NOTE-008'),
('COM-BANK-002','BANK','P-TB-001','提供供应商付款工具对照','2026-07-14','OPEN','NOTE-008');
