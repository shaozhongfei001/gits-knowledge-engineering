# P19 ｜ Protocol

## 生效规则

- 证据先于结论：任何 Gate 状态必须由实证（测试/脚本输出）支撑，禁止自我声称。
- 三同步：改 Java 枚举 / OpenAPI / SQL seed / Mapper XML / 前端 API 时，必须跑 `make check`（含 enum-consistency 防护网）。
- 失败先记录：新缺陷先写 `FAILURES.md` 再修复。
- QA_PASS 只能由独立 QA 角色记录；开发/Tech Lead 记录 DEV_SELF_CHECK_PASS。

## 环境协议

| 约束 | 命令 |
|---|---|
| 后端端口 8082（8080 被 SearXNG 占用） | `./mvnw -pl apps/api spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"` |
| 前端端口 5173 | `cd frontend && npx vite --host` |
| Maven 必须用 wrapper | `./mvnw`（系统 3.6.3 不满足） |
| E2E 验证 | `bash scripts/e2e-29-endpoints.sh [BASE_URL]` |
