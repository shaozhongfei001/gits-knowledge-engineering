-- V018: Customer seed data for demo
-- P19: 添加客户主档种子数据，确保 Journey Start 和 Customer API 可用

INSERT INTO customer (customer_id, customer_name, customer_short_name, unified_social_credit_code,
    established_date, registered_capital_cny, industry, region, enterprise_scale,
    customer_tier, relationship_since, rm_id, rm_name, managing_branch,
    group_flag, listed_status, risk_level, main_products, core_tags, relationship_summary,
    created_at, updated_at)
VALUES ('CUST-CORP-0001', '华东精工装备集团有限公司', '华东精工集团', '91330000MA27DEMO',
    '2005-03-15', 500000000, 'MANUFACTURING', '浙江省杭州市', 'LARGE',
    'STRATEGIC', '2018-06-01', 'RM-ZW-001', '张伟', '杭州城西支行',
    TRUE, 'UNLISTED', 'MEDIUM', '["精密加工","智能装备","自动化产线"]', '["制造业","出口导向","技改需求"]',
    '战略客户，集团本部及3家子公司在我行开户，综合授信额度1.5亿',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO customer (customer_id, customer_name, customer_short_name, unified_social_credit_code,
    established_date, registered_capital_cny, industry, region, enterprise_scale,
    customer_tier, relationship_since, rm_id, rm_name, managing_branch,
    group_flag, listed_status, risk_level, main_products, core_tags, relationship_summary,
    created_at, updated_at)
VALUES ('CUST-CORP-0002', '深圳创新科技有限公司', '深圳创新', '91440300MA5FXXX8B',
    '2012-07-20', 20000000, 'TECHNOLOGY', '华南', 'SMALL',
    'GROWTH', '2021-03-15', 'RM-002', '李晓华', '深圳南山支行',
    FALSE, 'UNLISTED', 'MEDIUM', '["软件开发","云计算服务","AI解决方案"]', '["科技型","高成长","融资需求"]',
    '成长型科技客户，近3年营收复合增长率35%，有科创板上市计划',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO customer (customer_id, customer_name, customer_short_name, unified_social_credit_code,
    established_date, registered_capital_cny, industry, region, enterprise_scale,
    customer_tier, relationship_since, rm_id, rm_name, managing_branch,
    group_flag, listed_status, risk_level, main_products, core_tags, relationship_summary,
    created_at, updated_at)
VALUES ('CUST-CORP-0003', '北京绿源环保集团', '绿源环保', '91110000MA1FL8XX5N',
    '2008-11-10', 100000000, 'ENERGY', '华北', 'LARGE',
    'KEY', '2015-09-01', 'RM-001', '张明远', '北京朝阳支行',
    TRUE, 'LISTED', 'LOW', '["污水处理","固废处理","环境监测"]', '["环保","PPP项目","政府合作"]',
    '重点集团客户，旗下5家子公司，环保行业龙头，PPP项目经验丰富',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
