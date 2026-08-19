# LLM WIKI 知识工程地图/目录 — 需求分析与合同候选草案（v0.2 对齐规范）

```text
DOC_ID=LLM-WIKI-KNOWLEDGE-MAP-002
STATUS=DRAFT_CANDIDATE（待 Owner / 合同审查后登记 CONTRACT_INDEX 方可生效）
AUTHOR=tech_lead
CREATED_AT=2026-08-19
UPDATED_AT=2026-08-19（v0.2 对齐《银行知识工程规范打样_fixed.xlsx》）
AUTHORITY=docs/dd/银行知识工程规范打样_fixed.xlsx（最高权威，本文档以规范为准）
BASELINE=main @ fd12d6f（P19/P20/P21 三 Loop 已 qa_pass 合并）
```

> 本文件是**需求分析与合同候选草案**，仅用于范围澄清与 Owner 决策，**不包含实现代码，不修改 `specs/` 或 `generated/`，不登记 `CONTRACT_INDEX.yaml`**。
> **权威来源声明**：本文档的知识条目（KI）、知识要素（KE）、任务（TASK）、规则（RUL）、技能（SK）、工具（T）、数据集（DS）、记忆反馈（MEM）等定义，一律以《银行知识工程规范打样_fixed.xlsx》为准，不发明规范未定义的结构。

---

## 1. 需求定义（Owner 原话转译）

> **LLM WIKI**：业务场景 → 流程任务 → 绑定知识领域 → 知识条目 → 知识要素 的知识工程地图和目录；**大模型要优先读这个地图再去执行任务**。

目标能力拆解：

| # | 能力 | 规范支撑 |
|---|---|---|
| K1 | 分层知识地图 | 规范 1.2-映射链：场景→知识域→知识条目→知识要素 |
| K2 | 目录可导航 | 规范 1.2 + 2.2：任务绑定 KI，KI 挂 KE，全链可下钻/反查 |
| K3 | 知识要素原子化 | 规范 4.4：KE 为原子要素，类型 K-Type-F/R/P/E/M |
| K4 | 大模型优先读图再执行 | 规范 5.2/5.3：Skill 能力包 + 场景提示词模板 |
| K5 | 受控与可追踪 | 规范：来源参考、关联规则、四类资产、fail-closed 不变量 |

---

## 2. 规范定义的权威模型（对齐基准）

### 2.1 层级结构（规范 SSOT）

```
业务场景（经营与营销-智能访前作战单）
  └─ 知识域 KD-01/02/04/05/06/08（客户/产品/流程/风险/合规/数据与智能）
       └─ 知识条目 KI（7个）：KI-009 企业客户基本信息, KI-FRONT-001~006
            └─ 知识要素 KE（每个KI下N个原子要素）
                 ├─ K-Type-F  事实/工具数据（从CRM/核心/外部拉取）
                 ├─ K-Type-R  规则（规则引擎产出，须形式化到 RUL）
                 ├─ K-Type-P  话术/方法（LLM 生成的提问/行动计划）
                 ├─ K-Type-E  综合研判（LLM 多源推理结论）
                 └─ K-Type-M  量化模型（量化计算评分）
  ├─ 任务 TASK-FRONT-000~007（一个任务绑定一个 KI；TASK-000 编排例外）
  ├─ 规则 RUL-FRONT-001~004（形式化规则，K-Type-R 必须落到 RUL）
  ├─ 工具 T-CRM/T-CORE/T-EXT/T-MARKET/T-LLM/T-PRODUCT/T-AUTH（REST/MCP/SDK）
  ├─ Skill SK-FRONT-000~007（能力包：编排/信息组装/图谱/八维/对账/话术/缺口/产品）
  ├─ 数据集 DS-FRONT-001 + Frozen 评测集（训练/评测隔离）
  └─ 记忆反馈 MEM-FRONT-001（历史报告 + 客户经理反馈回流）
```

### 2.2 关键定义（以规范实验例为准）

| 概念 | 编码规则 | 示例 | 规范 Sheet |
|---|---|---|---|
| 知识域 | `KD-xx` | KD-01 客户知识域 | 1.2 |
| 知识条目 | `KI-xxx` | KI-009 企业客户基本信息 | 4.3 |
| 知识要素 | `KE-{KI}-xx` | KE-009-01 客户全称 | 4.4 |
| 任务 | `TASK-FRONT-xxx` | TASK-FRONT-001 客户基本信息装配 | 2.1 |
| 规则 | `RUL-FRONT-xxx` | RUL-FRONT-001 事实对账冲突检测 | 4.5 |
| 技能 | `SK-FRONT-xxx` | SK-FRONT-001 信息组装 | 5.2 |
| 工具 | `T-xxx` | T-CRM-001 | 5.1 |
| 数据集 | `DS-FRONT-xxx` | DS-FRONT-001 | 4.1 |
| 记忆反馈 | `MEM-FRONT-xxx` | MEM-FRONT-001 | 1.2 |

### 2.3 知识要素类型（规范定义，非自创）

`K-Type-F / R / P / E / M`（以及组合如 `K-Type-F/R`）——按**要素生产方式/来源**分类，不是按"内容语义"分类。这是规范的权威口径。

---

## 3. 规范与 P20 现有模型的关系（对齐策略）

### 3.1 分层定位

| 层次 | 承担者 | 内容 |
|---|---|---|
| **知识语义层（本需求新增/对齐）** | 规范 KI/KE 体系 | "银行有什么知识"：KD/KI/KE（业务语义本体） |
| **运行时控制面（P20 已有）** | KnowledgeMap / AssetManifest / ActivationContract / RoutePolicy / ActivationPlan | "知识如何按权限路由执行" |

两者**不冲突、互补分层**。对齐目标：让 P20 的运行时模型能**承载/索引**规范的 KI/KE 语义，而非重写 P20 引擎。

### 3.2 逐项映射（规范 → P20）

| 规范概念 | P20 现有 | 对齐方式 | 影响 |
|---|---|---|---|
| 场景 | `KnowledgeMap.mapType=ROOT` + entrypoints | 场景可作为 ROOT map | 低 |
| 知识域 KD | `KnowledgeMap.domains[]`（domainId） | KD 编码可映射到 domainId | 低 |
| 知识条目 KI | `AssetManifest`（四类资产） | **KI 是业务语义条目，AssetManifest 是技术资产**——需在 AssetManifest 增加或关联 KI 标识 | **中：需扩展/映射** |
| 知识要素 KE | **无** | **新增 `knowledge-element` 合同**（`CTR-KELEM-001` 候选） | **高：新增** |
| 任务 TASK | `ActivationContract.taskType` | 一个 TASK 绑定一个 KI，对齐 taskType | 中 |
| 规则 RUL | `RoutePolicy.rules` / ActivationContract.ruleChecks | RUL 形式化到 P20 rule 体系 | 中 |
| 技能 SK | `skill-descriptor.schema.json`（SK-xxx） | **已对齐**（SK-FRONT 前缀一致） | 低 |
| 工具 T | 工具在 skill/activation 引用 | 对齐 | 低 |
| 数据集 DS / 记忆 MEM | 评测/反馈基础设施 | 对齐 | 低 |

### 3.3 对齐影响结论

- **新增**：`KnowledgeElement`（KE）合同、`KnowledgeElementPort`、`KnowledgeWikiPort`（渲染 LLM 可读地图/目录）。
- **扩展（向后兼容）**：`AssetManifest` 增加 `knowledgeItemId`（KI 引用）字段；`ActivationContract`/`KnowledgeMap` 增加 KI/KE 引用。
- **复用**：`KnowledgeMapPort`/`AssetCatalogPort`/`ActivationContractPort`/`RoutePolicyPort`/`SkillDescriptor` 沿用 P20 成果。
- **不新建**：`MetadataCatalogPort`（按先前结论复用 `AssetCatalogPort`）。

> ⚠️ 合同变更影响：扩展 `AssetManifest`/`ActivationContract`/`KnowledgeMap` 属于**合同变更**，涉及 `explicit_migration` 兼容性（`CTR-ASSET-001`/`CTR-ACTIVATION-001` 已是 explicit_migration）。需 Owner/合同审查批准后，按"改合同 → `make generate` → `make check`"执行。

---

## 4. 合同候选

### 4.1 新增 `KnowledgeElement`（知识要素）— 合同候选 `CTR-KELEM-001`

以规范 4.4 为准，字段对齐（KI 挂载、K-Type 枚举、来源参考、关联规则）：

```json
{
  "$id": "https://gientech.com/gits/kno/schemas/knowledge-element.json",
  "title": "GITS Atomic Knowledge Element",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "schemaVersion", "elementId", "name", "kind",
    "knowledgeItemId", "content", "source", "status"
  ],
  "properties": {
    "schemaVersion": {"type": "string", "const": "1.0.0"},
    "elementId":    {"type": "string", "pattern": "^KE-[A-Z0-9-]+-\\d{2}$"},
    "name":         {"type": "string", "minLength": 1},
    "kind": {
      "type": "string",
      "enum": ["K-Type-F","K-Type-R","K-Type-P","K-Type-E","K-Type-M"]
    },
    "knowledgeItemId": {"type": "string", "pattern": "^KI-[A-Z0-9-]+$"},
    "content":         {"type": "string", "minLength": 1},
    "source": {
      "type": "object",
      "required": ["sourceRef","authority"],
      "properties": {
        "sourceRef":   {"type": "string", "minLength": 1},
        "authority":   {"type": "string", "enum": ["AUTHORITATIVE","REFERENCE","DERIVED","SYNTHETIC"]}
      }
    },
    "relatedRules": {"type": "array", "items": {"type": "string", "pattern": "^RUL-"}},
    "status":       {"type": "string", "enum": ["DRAFT","VALIDATION","ACTIVE","RETIRED"]}
  }
}
```

设计要点：
- `kind` 使用规范的 **K-Type-F/R/P/E/M** 枚举（权威口径）。
- `knowledgeItemId` 挂到 KI（`KI-xxx`），形成 条目→要素 一级。
- `relatedRules` 引用 `RUL-xxx`（对齐规范 4.4 的"关联规则"列）。
- `elementId` 模式 `KE-{KI前缀}-{NN}`（对齐规范 `KE-009-01` 等示例）。

### 4.2 新增 Port 候选

| Port | 职责 | 方法候选 | 实现候选 | 状态 |
|---|---|---|---|---|
| `KnowledgeElementPort` | 原子知识要素读取（fail-closed） | `Optional<KnowledgeElement> find(elementId)`；`List<KnowledgeElement> listByKnowledgeItem(kiId)` | filesystem | 候选 |
| `KnowledgeWikiPort` | 渲染"LLM 可读的知识地图/目录/要素"（按 taskType/KD/KI 拉取） | `String renderMap(scope)`；`String renderKnowledgeItem(kiId)`；`String renderElement(elementId)` | filesystem（shadow）；真实平台（待 Owner 定选型） | 候选，**选型留空** |

### 4.3 复用（不新建）

- `KnowledgeMapPort` / `AssetCatalogPort` / `ActivationContractPort` / `RoutePolicyPort`：沿用 P20 成果。
- `AssetManifest`：扩展 `knowledgeItemId`（KI 引用），作为"知识条目"到运行时资产的桥。
- `SkillDescriptor`：规范 `SK-FRONT-xxx` 已对齐 `skill-descriptor.schema.json`。

---

## 5. "大模型优先读图再执行"机制 — 三选一（待 Owner 决）

现有 P20 `DefaultActivationPlanner` 用 Java 代码装配 `ActivationPlan`，**不注入 LLM**。让"LLM 先读地图再执行"的三候选：

| 方案 | 机制 | 优点 | 缺点 | 复杂度 |
|---|---|---|---|---|
| **A. 规划器决定加载范围**（倾向） | 扩展 planner → 产出待注入地图/目录片段 → `KnowledgeWikiPort.renderXxx` 渲染 → `LlmClient` 注入 | 保持 fail-closed 决策在 Java 侧可控；LLM 只读受控上下文 | 需改 planner + 新增渲染/注入链路 | 中 |
| B. 每次任务前加载相关子图 | `LlmClient` 按 taskType 先拉子图再执行 | 简单直接 | 脱离规划器权限/路由约束，弱化受控性 | 低 |
| C. 一次性加载全图 | 会话开始加载整图 | 上下文完整 | token 消耗大；越权风险 | 低 |

> **待决项**：倾向 **方案 A**（与 P20 fail-closed 不变量最一致），最终由 Owner 拍板。选型不臆测。

---

## 6. 需要 Owner 拍板的决策点清单

| # | 决策点 | 候选 | 状态 |
|---|---|---|---|
| D1 | 立项方式 | 新 Loop（建议 P22） | 待决 |
| D2 | 规范权威 | 以《银行知识工程规范打样_fixed.xlsx》为最高权威 | ✅ 已确认 |
| D3 | 知识要素粒度 | 原子知识要素（K-Type-F/R/P/E/M，规范口径） | ✅ 已确认 |
| D4 | 大模型读图机制 | A（规划器决定）/ B / C | 待决（倾向 A） |
| D5 | `AssetManifest` 扩展 | 新增 `knowledgeItemId`（KI 引用） | 待决 |
| D6 | `MetadataCatalogPort` | 复用 `AssetCatalogPort`，不新建 | ✅ 已确认 |
| D7 | 合同变更范围 | 新增 KE + 扩展 AssetManifest/ActivationContract/KnowledgeMap | 待决（explicit_migration） |
| D8 | 真实平台选型（RAG/图/元数据） | **留空，Owner 指定** | 待决 |

---

## 7. 边界与红线

- 本草案不写实现、不登记 `CONTRACT_INDEX`、不改 `specs/`、不改 `generated/`。
- 规范（xlsx）是最高权威；任何 KI/KE/RUL/SK/T/DS/MEM 定义均以规范为准，不发明结构。
- 合同变更（D5/D7）涉及 `explicit_migration`，需 Owner/合同审查批准后按合同流程执行。
- 真实平台接入选型未定，不得臆测实现。
- Oracle 写回等 quarantine 资产不并入本需求。
- 生产 cutover / fusion 需 Owner 单独批准（当前 preparation/shadow only）。
- 开发角色只记录 `DEV_SELF_CHECK`；正式 QA_PASS 由独立 QA 记录。

---

## 8. 变更记录

| 版本 | 时间 | 说明 |
|---|---|---|
| v0.1 | 2026-08-19 | 初稿：以 P20 模型为参考的需求分析与合同候选 |
| v0.2 | 2026-08-19 | **对齐《银行知识工程规范打样_fixed.xlsx》**：KI/KE 体系改为规范口径，要素类型改用 K-Type-F/R/P/E/M，新增规范↔P20 分层映射，修正自创枚举 |
