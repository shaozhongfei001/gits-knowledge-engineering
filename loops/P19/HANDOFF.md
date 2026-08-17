# P19 Baton 交接

> Loop: P19 | 交接日期: 2026-08-14 | 交接人: Tech Lead

## 当前状态

- P19 gates: G1/G2/G3 done, G4 E2E in_progress (脚本验证 29/29 PASS)
- 防护网已建立并接入 `make check`（enum_consistency + secret-scan 噪音治理）
- 真实缺陷已修复（外联脚本 NPE）
- 演示链路打通：后端 8082 + 前端 5173 连通

## 环境关键约束（必须知晓）

| 约束 | 说明 |
|------|------|
| 端口 8080 被 SearXNG 占用 | 后端**不能用 8080**，当前用 8082 启动 |
| Maven 版本 | 系统 3.6.3 不满足，必须用 `./mvnw` (3.9.12) |
| 后端启动命令 | `./mvnw -pl apps/api spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"` |
| 前端启动命令 | `cd frontend && npx vite --host` |
| 前端代理 | `vite.config.ts` proxy `/api` → `127.0.0.1:8082` |

## 待办（下一 Holder: 独立 QA 角色）

1. **记录 QA_PASS**：独立 QA 角色执行 `make verify` + `scripts/e2e-29-endpoints.sh`，验证后记录 `independent_qa.status=pass`（当前 EVIDENCE.json 中为 pending，因开发角色不得自签 QA_PASS）
2. **验证前端四态**：在浏览器 (localhost:5173) 验证 Dashboard / EngagementWorkspace 各组件 Loading/Success/Error 状态
3. **提交**：待 QA 通过后，将以下文件提交：
   - `scripts/enum_consistency_check.py`（新防护网）
   - `scripts/e2e-29-endpoints.sh`（新 E2E 验证）
   - `scripts/secret_scan.py`（噪音治理）
   - `Makefile`（check 目标新增 enum_consistency）
   - `apps/api/.../EngagementJourneyController.java`（NPE 修复）
   - `frontend/vite.config.ts`（端口对齐 8082）
   - `loops/P19/`（治理文档）

## 防护网使用说明

- `make check` 现在包含 enum-consistency 检查，任何改 seed SQL / Java 枚举必须跑 `make check`
- `make check` 不再因 secret-scan 误报而永久失败（告警 advisory 不阻塞，真实密钥仍 fail-closed）
- E2E 验证用 `bash scripts/e2e-29-endpoints.sh [BASE_URL]`

## 回退

若 QA 发现阻塞级问题，记录到 `FAILURES.md` 并退回 Feature Pilot；否则关闭 P19 Loop。
