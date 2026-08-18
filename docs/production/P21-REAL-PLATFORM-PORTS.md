# P21｜真实平台接入 Port 合同计划

```text
DOC_ID=P21-REAL-PLATFORM-PORTS-001
STATUS=CANDIDATE（待登记 CONTRACT_INDEX 后生效）
AUTHOR=tech_lead
CREATED_AT=2026-08-19
```

> 本文件定义生产就绪所需的真实平台接入 Port 合同候选。**候选合同须经 Owner/合同审查后登记 `CONTRACT_INDEX.yaml` → `make generate` → `make check`，禁止直接实现。**

## 候选 Port 合同

### 1. `RagEmbeddingPort`（real RAG）

- **职责**: 将资产文本 / 语义查询编码为向量并检索 Top-K 相关段落。
- **方法**:
  - `EmbeddedDoc embed(String text)` — 生成向量 + 元数据。
  - `List<RetrievedChunk> search(String query, int topK, RetrievalFilter filter)` — 语义检索。
- **实现候选**: `MilvusRagAdapter`（生产）；`MockRagAdapter`（shadow，P20 已有）。
- **隔离**: 不保存原始 KYC / 交易明细；仅投影/检索元数据。

### 2. `KnowledgeGraphPort`（GraphDB）

- **职责**: 资产、实体、关系的图查询（本体运行时投影）。
- **方法**:
  - `GraphNode getEntity(String iri)`
  - `List<GraphNode> queryNeighbors(String iri, String relation, int depth)`
- **实现候选**: `Neo4jGraphAdapter`（生产）；shadow 用 filesystem 地图。
- **隔离**: 仅承载本体投影，非运营对象权威状态库。

### 3. `MetadataCatalogPort`（OpenMetadata）

- **职责**: 资产清单 / Schema / 数据血缘目录（元数据投影）。
- **方法**:
  - `List<AssetMeta> listAssets(AssetFilter filter)`
  - `AssetMeta getAsset(String id)`
- **实现候选**: `OpenMetadataAdapter`（生产）。
- **隔离**: 仅元数据，不含权限决策。

## 登记流程（后续）

1. 候选 Port 经合同审查（ADR）后登记 `CONTRACT_INDEX.yaml`。
2. `make generate` 生成 Port 骨架 → `make check` 校验哈希。
3. 再实现适配器 + 集成测试。
4. 独立 QA 验证。
5. Owner 批准实际接入（不属 P21 准备阶段执行）。

## 不变量（继承 ADR-0017 / ADR-0015）

- 真实平台是**投影层**，不成为权威源 / 权限引擎 / 本体运行时。
- 权威源始终为 `specs/` 语义合同。
- Oracle 写回属 quarantine 资产，需单独授权，不并入本批 Port。
