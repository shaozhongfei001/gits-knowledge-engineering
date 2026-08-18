# P19 Baton 交接

> Loop: P19 | 交接日期: 2026-08-19 | 交接人: Independent QA

## 当前状态

- P19 status=`qa_pass`，qa_actor=`independent_qa`，`qa_attest.py` EXIT=0
- gates: G1_CONTRACT_ALIGN / G2_BACKEND_FIX / G3_FRONTEND_FIX / G4_E2E_VERIFY 全 done
- 防护网已建立并接入 `make check`（enum_consistency + secret-scan 噪音治理）
- 真实缺陷已修复（外联脚本 NPE）
- 演示链路打通：后端 8082 + 前端 5173 连通
- loop-guard evidence + memory 均 PASS；Baton 已交接至 `independent_qa`
- 独立 QA 复现 `make verify` 全绿（除 db-check 外部凭据环境依赖，见 FAILURES.md E-1）

## 环境关键约束（必须知晓）

| 约束 | 说明 |
|------|------|
| 端口 8080 被 SearXNG 占用 | 后端**不能用 8080**，当前用 8082 启动 |
| Maven 版本 | 系统 3.6.3 不满足，必须用 `./mvnw` (3.9.12) |
| 后端启动命令 | `./mvnw -pl apps/api spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"` |
| 前端启动命令 | `cd frontend && npx vite --host` |
| 前端代理 | `vite.config.ts` proxy `/api` → `127.0.0.1:8082` |

## 待办（已由 Independent QA 完成，移交 Owner）

1. **记录 QA_PASS**：已完成 — 独立 QA 执行 `make verify`（复现全绿）+ 正式 `qa_attest.py --actor independent_qa --decision pass` EXIT=0，`independent_qa.status=pass`
2. **验证前端四态**：已完成 — 前端 vue-tsc + vitest 100 tests + vite build 全 PASS（E2E 由 playwright 单独运行）
3. **提交**：待 QA 通过后提交（P19 相关代码/脚本/文档）：
   - `scripts/enum_consistency_check.py`（新防护网）
   - `scripts/e2e-29-endpoints.sh`（新 E2E 验证）
   - `scripts/secret_scan.py`（噪音治理）
   - `Makefile`（check 目标新增 enum_consistency）
   - `apps/api/.../EngagementJourneyController.java`（NPE 修复）
   - `frontend/vite.config.ts`（端口对齐 8082）
   - `loops/P19/`（治理文档）
   - 本批次另含 scope 外前端修复：`frontend/src/api/__tests__/engagement.spec.ts`、`frontend/vitest.config.ts`（FAILURES.md F-4）
4. **Owner P5 审查**：审阅正式 QA + 全量回归，决定受控合并（不表示生产就绪/冻结）

## 防护网使用说明

- `make check` 现在包含 enum-consistency 检查，任何改 seed SQL / Java 枚举必须跑 `make check`
- `make check` 不再因 secret-scan 误报而永久失败（告警 advisory 不阻塞，真实密钥仍 fail-closed）
- E2E 验证用 `bash scripts/e2e-29-endpoints.sh [BASE_URL]`

## 回退

若 QA 发现阻塞级问题，记录到 `FAILURES.md` 并退回 Feature Pilot；否则关闭 P19 Loop。
