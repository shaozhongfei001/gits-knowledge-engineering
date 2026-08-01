.PHONY: help bootstrap-check generate check contract-diff security-check framework-test tooling-test backend-test frontend-test db-check db-init verify new-loop memory-check evidence-check dry-run

PYTHON ?= python3
MVNW ?= ./mvnw
JENA_VERSION := $(shell grep -oE '<jena.version>[^<]+</jena.version>' pom.xml | sed -E 's/<\/?jena.version>//g')
LOOP ?=
HOLDER ?=
BASELINE ?=

help: ## 显示命令
	@awk 'BEGIN {FS = ":.*## "}; /^[a-zA-Z0-9_-]+:.*## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

bootstrap-check: ## 严格检查Java/Maven/Node/Python/Git/ripgrep环境
	@bash scripts/bootstrap-check.sh

generate: ## 从全部权威合同源生成只读制品
	@test -x scripts/generate-contracts.sh || { echo "FAIL: scripts/generate-contracts.sh missing or not executable"; exit 2; }
	@bash scripts/generate-contracts.sh

check: ## 验证合同、生成物、Loop模板和安全基线
	@test -x scripts/check-contracts.sh || { echo "FAIL: scripts/check-contracts.sh missing or not executable"; exit 2; }
	@bash scripts/check-contracts.sh
	@$(PYTHON) scripts/loop_guard.py --template-check
	@$(PYTHON) scripts/secret_scan.py --root .

contract-diff: ## 与指定基线目录比较兼容性：BASELINE=path
	@test -n "$(BASELINE)" || { echo "FAIL: BASELINE is required"; exit 2; }
	@$(PYTHON) scripts/contract_diff.py --baseline "$(BASELINE)" --current generated

security-check: ## 秘密、敏感目录权限和隔离资产检查
	@$(PYTHON) scripts/secret_scan.py --root .
	@$(PYTHON) scripts/sensitive_permissions.py --root .
	@$(PYTHON) tools/quarantine/oracle/readonly_guard.py --self-test

framework-test: ## 安装器plan/apply/idempotency/conflict/rollback测试
	@$(PYTHON) -m unittest discover -s tools/sdd-installer/tests -v

tooling-test: ## 合同、Loop、安全与Oracle guard单元测试
	@$(PYTHON) -m unittest discover -s tests/tooling -v

backend-deps-check: ## 验证semantic-jena的Jena依赖可从根统一治理解析
	@test -x $(MVNW) || { echo "FAIL: $(MVNW) missing or not executable"; exit 2; }
	@$(MVNW) --batch-mode --no-transfer-progress -pl adapters/semantic-jena -am dependency:tree -Dincludes=org.apache.jena:jena-arq,org.apache.jena:jena-shacl > /tmp/gits-deps.tree 2>&1 || { cat /tmp/gits-deps.tree; echo "FAIL: dependency:tree failed"; exit 2; }
	@grep -q 'org.apache.jena:jena-arq:jar:$(JENA_VERSION)' /tmp/gits-deps.tree && grep -q 'org.apache.jena:jena-shacl:jar:$(JENA_VERSION)' /tmp/gits-deps.tree || { cat /tmp/gits-deps.tree; echo "FAIL: jena-arq/jena-shacl not resolved to governed version $(JENA_VERSION)"; exit 2; }
	@echo "backend-deps-check: PASS"

backend-test: backend-deps-check ## Java 21/Maven真实编译和测试
	@test -x $(MVNW) || { echo "FAIL: $(MVNW) missing or not executable"; exit 2; }
	@$(MVNW) --batch-mode --no-transfer-progress verify

frontend-test: ## 前端锁文件安装、类型检查、测试和构建
	@test -f frontend/package-lock.json || { echo "FAIL: frontend/package-lock.json missing; lock dependencies before testing"; exit 2; }
	@cd frontend && npm ci && npm run check && npm run test && npm run build

db-check: ## 验证gits_ke管理库可连接可写(需GITS_KEDB_PASSWORD在仓库外设置)
	@bash scripts/db/db_check.sh

db-init: ## 用Flyway初始化/迁移gits_ke schema(需GITS_KEDB_PASSWORD在仓库外设置)
	@bash scripts/db/db_init.sh

verify: bootstrap-check generate check framework-test tooling-test backend-test frontend-test db-check ## 完整本地验证

new-loop: ## 创建批次：make new-loop LOOP=P1-xxx HOLDER=tech_lead
	@test -n "$(LOOP)" -a -n "$(HOLDER)" || { echo "FAIL: LOOP and HOLDER are required"; exit 2; }
	@$(PYTHON) scripts/new_loop.py --loop-id "$(LOOP)" --holder "$(HOLDER)"

memory-check: ## 检查Baton与共享记忆
	@test -n "$(LOOP)" || { echo "FAIL: LOOP is required"; exit 2; }
	@$(PYTHON) scripts/loop_guard.py --loop "$(LOOP)" --memory-only

evidence-check: ## 检查状态、命令和证据hash
	@test -n "$(LOOP)" || { echo "FAIL: LOOP is required"; exit 2; }
	@$(PYTHON) scripts/loop_guard.py --loop "$(LOOP)" --evidence-only

dry-run: generate check framework-test tooling-test ## 不连接真实外部系统的机制Dry-run
	@echo "DEV_SELF_CHECK_PASS: framework and repository mechanisms; independent QA still required"
