# ADR-0014: Scenario 目录聚合重组

| 状态 | 日期 | 决策者 |
|------|------|--------|
| 已批准 | 2026-08-12 | Tech Lead |

## 上下文

项目中场景相关资产分散在两个独立位置：
- `modules/scenario-hermes/`：Maven 子模块，包含 10 个领域 record、10 个 port 接口、6 个策略类——场景执行核心
- `scenario_data/`：252 个数据文件（JSON/JSONL/CSV/YAML/MD/SQL），17 个子目录——场景种子数据 SSOT

两者是同一业务领域（Demo 场景）的代码和数据两面，但目录结构上完全隔离，缺乏内聚性。

## 决策

采用 **父目录聚合** 方案，创建 `scenario/` 父目录：

```
scenario/
├── execute/     ← 原 modules/scenario-hermes/（Maven 子模块，artifactId: scenario-execute）
└── seed/        ← 原 scenario_data/（种子数据，非 Maven 模块）
```

### 关键变更

| 变更项 | 旧值 | 新值 |
|--------|------|------|
| Maven 模块路径 | `modules/scenario-hermes` | `scenario/execute` |
| Maven artifactId | `scenario-hermes` | `scenario-execute` |
| 种子数据目录 | `scenario_data/` | `scenario/seed/` |
| Java 包名 | `com.gien.gits.engagement` | 不变 |
| Dockerfile COPY 路径 | `modules/scenario-hermes/` | `scenario/execute/` |

### 不变项

- Java 包名（`com.gien.gits.engagement`）不变，避免全量 import 重写
- `scenario.data-root` 配置键名不变，仅默认路径语义更新
- 业务逻辑和 API 合同不变

## 理由

1. **内聚性**：代码（execute）和数据（seed）同属场景领域，父目录聚合使关系显式化
2. **可发现性**：新成员一眼看到 `scenario/` 就知道场景相关资产在此
3. **命名语义**：`execute` 比 `code` 更准确表达"场景执行引擎"的职责；`seed` 比 `data` 更准确表达"种子数据"的不可变性
4. **最小影响**：Java 包名不变，仅改 Maven 坐标和文件路径，影响面可控

## 受影响文件

- `pom.xml`（根）、`scenario/execute/pom.xml`、`apps/api/pom.xml`、`adapters/persistence-relational/pom.xml`、`modules/human-action/pom.xml`
- `Dockerfile`
- `application.yaml`（注释）
- `scripts/switch-scenario-version.sh`
- `ScenarioDataConfig.java`、`V11ScenarioDataReader.java`（注释）
- `JdbcXxxRepository.java`（Javadoc 注释）
- `AGENTS.md`（规则激活路径）

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| CI/CD 路径硬编码 | 已验证 Dockerfile 和脚本全部更新 |
| IDE 缓存失效 | 开发者需刷新 Maven 项目 |
| 文档中旧路径引用 | docs/ 下的 ADR 和设计文档保留历史记录，不强制更新 |
