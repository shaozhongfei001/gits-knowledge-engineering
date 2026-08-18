-- Bulk loader examples. Replace /ABS/PATH with this package's absolute path.
USE rm_continuous_demo;
SET GLOBAL local_infile = 1;

LOAD DATA LOCAL INFILE '/ABS/PATH/02_master_data/legal_entities.csv'
INTO TABLE legal_entity CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n' IGNORE 1 LINES
(entity_id,name,short_name,entity_type,role_desc,ownership_parent,ownership_pct,bank_customer_id,status);

LOAD DATA LOCAL INFILE '/ABS/PATH/03_bank_data/daily_account_balances.csv'
INTO TABLE daily_balance CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n' IGNORE 1 LINES
(@date,account_id,currency,eod_balance_cny,source_system,@as_of,visibility_scope)
SET date=STR_TO_DATE(@date,'%Y-%m-%d'), as_of=STR_TO_DATE(@as_of,'%Y-%m-%d');

-- Transaction CSV column date should be mapped to tx_date.
-- Prefer application-side CSV loader for robust quoting and idempotent upsert in dev.
