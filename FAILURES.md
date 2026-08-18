# FAILURES.md

## Mapper XML Record 适配修复 — E2E 验证失败记录

**验证日期**: 2026-08-12
**验证角色**: E2E Owner
**分支**: feature/v11-frontend-depth-remediation

---

### F-1: FactReconciliationCaseMapper.xml 引用不存在的内部类 [BLOCKER]

**严重程度**: BLOCKER — 阻断所有集成测试

**文件**: `adapters/persistence-relational/src/main/resources/mapper/foundation/ontology/FactReconciliationCaseMapper.xml`

**行号**: 21

**现状**:
```xml
<arg column="status" javaType="com.gien.gits.ontology.FactReconciliationCase$ReconciliationStatus"/>
```

**期望**:
```xml
<arg column="status" javaType="com.gien.gits.ontology.ReconciliationStatus"/>
```

**原因**: `ReconciliationStatus` 是顶级枚举类 (`com.gien.gits.ontology.ReconciliationStatus`)，不是 `FactReconciliationCase` 的内部类。

**影响**: MyBatis 解析 Mapper XML 失败 → SqlSessionFactory 创建失败 → 所有 43 个集成测试 ERROR。

---

### F-2: 6 个 Mapper XML 的 INSERT 语句中 Instant 字段缺少 InstantTypeHandler [MINOR]

**严重程度**: MINOR — H2 兼容模式下可能不阻塞，但与项目规范不一致

**规范要求**: 所有 `java.time.Instant` 字段在 INSERT/UPDATE 中必须指定 `typeHandler=com.gien.gits.adapter.persistence.common.typehandler.InstantTypeHandler`

| 文件 | 缺失字段 |
|------|----------|
| `RelationshipReportMapper.xml` | `createdAt`, `updatedAt` |
| `ProductKnowledgeCardMapper.xml` | `createdAt` |
| `BankRelationshipSnapshotMapper.xml` | `createdAt` |
| `FactReconciliationCaseMapper.xml` | `createdAt`, `updatedAt` |
| `CreditFacilityMapper.xml` | `createdAt`, `updatedAt` |
| `TransactionRecordMapper.xml` | `createdAt` |
| `GroupRelationshipMapper.xml` | `createdAt` |

**注意**: `OpportunityMapper.xml` 的 `expectedCloseDate` 是 `String` 类型，不需要 InstantTypeHandler（已排除）。

---

## 验证通过项

| 检查项 | 结果 |
|--------|------|
| 编译 + 单元测试 (persistence-relational -am) | ✅ 19 tests, 0 failures |
| 所有 33 个 Mapper XML 使用 `<constructor>` 模式 | ✅ 无残留 `<result property=...>` |
| PrevisitReportContentMapper productSchemes typeHandler 一致性 | ✅ resultMap 和 INSERT 均使用 ProductSchemeListTypeHandler |
| 其他 `$` 内部类引用 (17 处) | ✅ 全部为真实内部类/枚举 |
| 已有 INSERT 中 InstantTypeHandler 覆盖 | ✅ 大部分 Mapper 已正确添加 |

---

## 退回建议

退回至 Feature Pilot 修复 F-1 (BLOCKER) 和 F-2 (MINOR) 后重新验证。
