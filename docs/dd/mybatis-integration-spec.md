# MyBatis 引入开发规范（草案）

## 一、项目现状摘要

| 项目 | 现状 |
|------|------|
| 构建工具 | Maven 多模块 |
| 数据库 | H2（开发） / MySQL（生产） |
| ORM | 纯 `JdbcTemplate` + 手写 `RowMapper` |
| 仓储数量 | 23 个 `JdbcXxxRepository` |
| 仓储注册 | `RepositoryConfig` 手动 `@Bean` 注册 |
| 领域模型 | 全部 `record` 类型，无 setter |
| 架构 | 六边形架构 — `port`(接口) → `adapter`(实现) |
| JSON处理 | `JsonHelper` 工具类 + 部分内联 `ObjectMapper` |

---

## 二、依赖管理规范

**1. 版本统一管理** — 在根 `pom.xml` 的 `<dependencyManagement>` 中声明：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${mybatis-spring-boot.version}</version>
</dependency>
```

版本号选择原则：
- 与当前 Spring Boot 3.x 兼容 → `mybatis-spring-boot-starter` ≥ 3.0.x
- 在根 pom 的 `<properties>` 中定义 `<mybatis-spring-boot.version>3.0.4</mybatis-spring-boot.version>`

**2. 依赖声明位置** — 仅在 `adapters/persistence-relational` 模块引入：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

`apps/api` 模块通过传递依赖自动获得，不重复声明。

---

## 三、分层与包结构规范

### 核心原则：两层架构 — 领域基建层 + 业务子系统层

项目中的 `ontology`（运营本体）、`action`（人工行动）、`engagement`（客户触达场景）是**领域基建**，它们定义了核心领域模型和基础操作，被多个业务子系统复用。业务子系统（市场慧眼、客户360、KYC访前等）是在基建之上编排的业务能力。

因此持久化层采用**两层结构**：

| 层次 | 定位 | 组织依据 | 示例 |
|------|------|---------|------|
| **领域基建层** `foundation/` | 核心领域实体的 CRUD 持久化 | 对应 `modules/` 下的基建模块 | `foundation/ontology/` → ExternalEvent, Customer, Claim... |
| **业务子系统层** `business/` | 业务编排查询、跨实体组合查询 | 对应业务主链的业务环节 | `business/marketinsight/` → 市场洞察组合查询 |

**关键区分**：
- **基建层**：单实体的基础 CRUD，直接映射 Port 接口，与 `modules/` 基建模块一一对应
- **业务层**：跨实体的业务查询与组合，仅当业务需求超出单实体 CRUD 时才出现；业务层通过注入基建层 Service/Mapper 实现组合

### 领域基建层划分

| 基建包名 | 对应 Maven 模块 | 业务定位 | 涉及的核心领域模型 | 涉及的 Port 接口 |
|---------|---------------|---------|------------------|----------------|
| `foundation/ontology` | `modules/operational-ontology` | 运营本体 — 核心领域概念与事实 | ExternalEvent, Customer, Claim, CreditFacility, LegalEntity, GroupRelationship, KycGapProfile, Interaction, InteractionExtension, Commitment, OperatingCase, FactReconciliationCase, OpportunitySignal, BankRelationshipSnapshot, Transaction, TransactionRecord, PolicyRule, ProductKnowledgeCard, RelationshipReport, Opportunity, Evidence, EvidenceVersionLink, ClaimLifecycleEvent, ActionReceipt, ControlledAction, HumanConfirmation | WritableExternalEventRepository, WritableCustomerRepository, WritableClaimRepository, WritableCreditFacilityRepository, WritableLegalEntityRepository, WritableGroupRelationshipRepository, WritableKycGapProfileRepository, WritableInteractionRepository, WritableInteractionExtensionRepository, WritableCommitmentRepository, WritableOperatingCaseRepository, WritableFactReconciliationRepository, WritableOpportunitySignalRepository, WritableBankRelationshipSnapshotRepository, WritableTransactionRepository, WritableTransactionRecordRepository, WritablePolicyRuleRepository, WritableProductKnowledgeVersionRepository, WritableProductCatalogRepository, WritableRelationshipReportRepository, WritableOpportunityRepository, WritableEvidenceVersionLinkRepository, WritableClaimLifecycleRepository |
| `foundation/action` | `modules/human-action` | 人工行动 — 任务与确认 | Task, RecordingConsent | WritableTaskRepository, WritableRecordingConsentRepository |
| `foundation/engagement` | `modules/scenario-hermes` | 客户触达场景 — 访前/访中/访后内容 | PrevisitReportContent, MeetingScript, OutreachScript, PostvisitAnalysisContent, CrmWritebackCommand | WritablePrevisitReportContentRepository, WritableMeetingScriptRepository, WritableOutreachScriptRepository, WritablePostvisitAnalysisContentRepository |
| `foundation/journey` | `modules/scenario-customer-journey` | 经营旅程编排 | CustomerJourney | WritableCustomerJourneyRepository |

> **注意**：`operational-ontology` 模块体量最大（30+ 领域模型、23+ Port 接口），是整个系统的领域核心。
> 当该包下文件过多时，可按领域概念进一步拆分为子包（见下方"ontology 内部拆分"章节）。

### 业务子系统层划分

业务子系统层**仅在出现跨实体业务查询时**才创建，不强制为每个业务环节建包。

| 业务包名 | 业务含义 | 触发条件（何时创建） | 典型场景 |
|---------|---------|-------------------|---------|
| `business/marketinsight` | 市场慧眼 | 需要 ExternalEvent + OpportunitySignal + Customer 联合查询 | 市场机会聚合看板 |
| `business/customer360` | 客户360 | 需要 Customer + CreditFacility + GroupRelationship 联合查询 | 客户全景视图 |
| `business/kycprep` | KYC访前 | 需要 KycGapProfile + Claim + PolicyRule 联合查询 | KYC缺口分析报告 |
| `business/postvisit` | 访后分析 | 需要 PostvisitAnalysisContent + RelationshipReport + Opportunity 联合查询 | 访后综合报告 |
| `business/crmwriteback` | CRM写回 | 需要 ControlledAction + HumanConfirmation 联合查询 | 受控写回审批流 |

> **原则**：如果某个业务环节的所有查询都是单实体 CRUD，则不需要创建业务包，直接使用基建层即可。
> 业务包中的 Mapper 仅包含跨实体组合查询，不重复基建层已有的单实体 CRUD。

### ontology 内部拆分（当文件过多时）

`foundation/ontology/` 包含 30+ 领域模型，当 Mapper/Service 文件数量超过 15 个时，按领域概念分组：

```
foundation/ontology/
├── event/                          ← 外部事件与信号
│   ├── mapper/
│   │   ├── ExternalEventMapper.java
│   │   └── OpportunitySignalMapper.java
│   ├── service/
│   │   ├── ExternalEventService.java
│   │   └── OpportunitySignalService.java
├── customer/                       ← 客户与关联实体
│   ├── mapper/
│   │   ├── CustomerMapper.java
│   │   ├── CreditFacilityMapper.java
│   │   ├── LegalEntityMapper.java
│   │   └── GroupRelationshipMapper.java
│   ├── service/
│   │   ├── CustomerService.java
│   │   ├── CreditFacilityService.java
│   │   ├── LegalEntityService.java
│   │   └── GroupRelationshipService.java
├── claim/                          ← Claim 体系
│   ├── mapper/
│   │   ├── ClaimMapper.java
│   │   └── ClaimLifecycleMapper.java
│   ├── service/
│   │   ├── ClaimService.java
│   │   └── ClaimLifecycleService.java
├── interaction/                    ← 交互与事实对账
│   ├── mapper/
│   │   ├── InteractionMapper.java
│   │   ├── InteractionExtensionMapper.java
│   │   └── FactReconciliationMapper.java
│   ├── service/
│   │   ├── InteractionService.java
│   │   ├── InteractionExtensionService.java
│   │   └── FactReconciliationService.java
├── commitment/                     ← 承诺与任务
│   ├── mapper/
│   │   └── CommitmentMapper.java
│   ├── service/
│   │   └── CommitmentService.java
├── product/                        ← 产品与策略
│   ├── mapper/
│   │   ├── ProductCatalogMapper.java
│   │   ├── ProductKnowledgeVersionMapper.java
│   │   └── PolicyRuleMapper.java
│   ├── service/
│   │   ├── ProductCatalogService.java
│   │   ├── ProductKnowledgeVersionService.java
│   │   └── PolicyRuleService.java
├── evidence/                       ← 证据与关系报告
│   ├── mapper/
│   │   ├── EvidenceVersionLinkMapper.java
│   │   └── RelationshipReportMapper.java
│   ├── service/
│   │   ├── EvidenceVersionLinkService.java
│   │   └── RelationshipReportService.java
├── operating/                      ← 经营案例与机会
│   ├── mapper/
│   │   ├── OperatingCaseMapper.java
│   │   ├── OpportunityMapper.java
│   │   ├── BankRelationshipSnapshotMapper.java
│   │   ├── TransactionMapper.java
│   │   └── TransactionRecordMapper.java
│   ├── service/
│   │   ├── OperatingCaseService.java
│   │   ├── OpportunityService.java
│   │   ├── BankRelationshipSnapshotService.java
│   │   ├── TransactionService.java
│   │   └── TransactionRecordService.java
└── action/                         ← 受控行动
    ├── mapper/
    │   ├── ControlledActionMapper.java
    │   ├── HumanConfirmationMapper.java
    │   └── ActionReceiptMapper.java
    ├── service/
    │   ├── ControlledActionService.java
    │   ├── HumanConfirmationService.java
    │   └── ActionReceiptService.java
```

> **拆分时机**：初期不拆分，所有文件平铺在 `foundation/ontology/mapper/` 和 `foundation/ontology/service/` 下。
> 当同一目录下 Java 文件超过 15 个时，按上表分组拆分子包。

### Java 包结构（完整）

```
adapters/persistence-relational/src/main/java/com/gien/gits/adapter/persistence/
│
├── foundation/                            ← 领域基建层（对应 modules/ 基建模块）
│   ├── ontology/                          ← 运营本体 → modules/operational-ontology
│   │   ├── mapper/                        ← （初期平铺，超过15个文件时按领域概念拆子包）
│   │   │   ├── ExternalEventMapper.java
│   │   │   ├── CustomerMapper.java
│   │   │   ├── ClaimMapper.java
│   │   │   ├── CreditFacilityMapper.java
│   │   │   ├── InteractionMapper.java
│   │   │   ├── CommitmentMapper.java
│   │   │   ├── TaskMapper.java            ← Task 的 CRUD 也在 ontology（领域模型在此定义）
│   │   │   └── ...
│   │   ├── service/
│   │   │   ├── ExternalEventService.java  ← implements WritableExternalEventRepository
│   │   │   ├── CustomerService.java       ← implements WritableCustomerRepository
│   │   │   ├── ClaimService.java          ← implements WritableClaimRepository
│   │   │   └── ...
│   │
│   ├── action/                            ← 人工行动 → modules/human-action
│   │   ├── mapper/
│   │   │   └── RecordingConsentMapper.java
│   │   ├── service/
│   │   │   └── RecordingConsentService.java ← implements WritableRecordingConsentRepository
│   │
│   ├── engagement/                        ← 客户触达场景 → modules/scenario-hermes
│   │   ├── mapper/
│   │   │   ├── PrevisitReportContentMapper.java
│   │   │   ├── MeetingScriptMapper.java
│   │   │   ├── OutreachScriptMapper.java
│   │   │   └── PostvisitAnalysisContentMapper.java
│   │   ├── service/
│   │   │   ├── PrevisitReportContentService.java
│   │   │   ├── MeetingScriptService.java
│   │   │   ├── OutreachScriptService.java
│   │   │   └── PostvisitAnalysisContentService.java
│   │
│   └── journey/                           ← 经营旅程 → modules/scenario-customer-journey
│       ├── mapper/
│       │   └── CustomerJourneyMapper.java
│       ├── service/
│       │   └── CustomerJourneyService.java ← implements WritableCustomerJourneyRepository
│
├── business/                              ← 业务子系统层（跨实体业务查询，按需创建）
│   ├── marketinsight/                     ← 市场慧眼（ExternalEvent + OpportunitySignal + Customer 联合查询）
│   │   ├── mapper/
│   │   │   └── MarketInsightQueryMapper.java
│   │   ├── service/
│   │   │   └── MarketInsightQueryService.java
│   ├── customer360/                       ← 客户360（Customer + CreditFacility + GroupRelationship 联合查询）
│   │   ├── mapper/
│   │   ├── service/
│   ├── kycprep/                           ← KYC访前（KycGapProfile + Claim + PolicyRule 联合查询）
│   │   ├── mapper/
│   │   ├── service/
│   ├── postvisit/                         ← 访后分析
│   │   ├── mapper/
│   │   ├── service/
│   └── crmwriteback/                      ← CRM写回
│       ├── mapper/
│       ├── service/
│
├── common/                                ← 跨层共享基础设施
│   ├── typehandler/                       ← MyBatis TypeHandler
│   │   ├── EnumTypeHandler.java
│   │   ├── StringListJsonTypeHandler.java
│   │   ├── InstantTypeHandler.java
│   │   └── UuidListJsonTypeHandler.java
│   └── JsonHelper.java                    ← 保留，逐步迁移至 TypeHandler
│
├── v11/                                   ← 现有 JDBC 适配器（保留，逐步迁移）
│   ├── JdbcExternalEventRepository.java
│   ├── JdbcTaskRepository.java
│   └── ...
└── ...
```

### XML 映射文件结构

```
adapters/persistence-relational/src/main/resources/
├── db/migration/                          ← Flyway 迁移（不动）
└── mapper/                                ← 与 Java 包结构对齐
    ├── foundation/
    │   ├── ontology/
    │   │   ├── ExternalEventMapper.xml
    │   │   ├── CustomerMapper.xml
    │   │   ├── ClaimMapper.xml
    │   │   └── ...
    │   ├── action/
    │   │   └── RecordingConsentMapper.xml
    │   ├── engagement/
    │   │   ├── PrevisitReportContentMapper.xml
    │   │   ├── MeetingScriptMapper.xml
    │   │   ├── OutreachScriptMapper.xml
    │   │   └── PostvisitAnalysisContentMapper.xml
    │   └── journey/
    │       └── CustomerJourneyMapper.xml
    └── business/                          ← 按需创建
        ├── marketinsight/
        │   └── MarketInsightQueryMapper.xml
        └── ...
```

### 实体归属原则

| 场景 | 归属 | 说明 |
|------|------|------|
| 实体定义在 `modules/operational-ontology` | `foundation/ontology/` | 如 ExternalEvent, Customer, Claim, Interaction, Commitment, Task... |
| 实体定义在 `modules/human-action` | `foundation/action/` | 如 RecordingConsent（Task 的领域模型在 ontology，但 Port 在 action） |
| 实体定义在 `modules/scenario-hermes` | `foundation/engagement/` | 如 PrevisitReportContent, MeetingScript, OutreachScript |
| 实体定义在 `modules/scenario-customer-journey` | `foundation/journey/` | 如 CustomerJourney |
| 跨实体业务查询 | `business/{subsystem}/` | 仅当查询跨越多个基建实体时才创建 |
| 同一实体在不同业务场景有不同查询 | 基建层放通用 CRUD，业务层放场景化查询 | 基建层 Mapper 提供基础方法，业务层 Mapper 提供组合查询 |

> **特殊处理 — Task**：`Task` 的领域模型定义在 `ontology`，但 `WritableTaskRepository` Port 接口在 `action`。
> 持久化归属 `foundation/ontology/`（跟随领域模型定义位置），`foundation/action/` 的 Service 可注入 ontology 的 TaskService。
> 如果未来 `action` 模块有 Task 专属的业务查询，可在 `foundation/action/mapper/` 中扩展。

---

## 四、命名规范

| 概念 | 命名模式 | 示例 |
|------|----------|------|
| 领域基建包 | `foundation/{module}` | `foundation/ontology`, `foundation/action`, `foundation/engagement`, `foundation/journey` |
| 业务子系统包 | `business/{subsystem}` | `business/marketinsight`, `business/customer360`, `business/kycprep` |
| Mapper 接口 | `{Entity}Mapper` | `ExternalEventMapper` |
| 业务查询 Mapper | `{Subsystem}QueryMapper` | `MarketInsightQueryMapper` |
| XML 映射文件 | `{Entity}Mapper.xml` | `ExternalEventMapper.xml` |
| 持久化服务 | `{Entity}Service` | `ExternalEventService` |
| Mapper 命名空间 | 全限定类名 | `com.gien.gits.adapter.persistence.foundation.ontology.mapper.ExternalEventMapper` |
| 查询方法 | `find`/`select` 前缀 | `findByEventId`, `selectRecent` |
| 写入方法 | `insert`/`update`/`delete` 前缀 | `insertExternalEvent`, `updateStatus` |

> **注意**：持久化服务 `ExternalEventService` 与 API 层的 `ExternalEventService`（`apps/api/.../service/`）属于不同层次、不同模块，包路径天然隔离，不会冲突。

---

## 五、Mapper 接口规范

**1. 接口定义** — 纯接口，不加 `@Mapper` 注解（由 `@MapperScan` 统一扫描）：

```java
package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.ExternalEvent;
import java.util.List;
import java.util.Optional;

public interface ExternalEventMapper {

    void insert(ExternalEvent event);

    Optional<ExternalEvent> findByEventId(String eventId);

    List<ExternalEvent> findBySourceType(String sourceType);

    List<ExternalEvent> findByEntity(String entity);

    List<ExternalEvent> findRecent(int limit);

    List<ExternalEvent> findAll();
}
```

**2. 方法参数** — 单参数直接传领域对象/值；多参数用 `@Param`：

```java
List<ExternalEvent> findBySourceTypeAndEntity(
    @Param("sourceType") String sourceType,
    @Param("entity") String entity);
```

**3. 返回类型** — 直接返回领域 `record` 类型，不引入 DTO/PO 中间层。

---

## 六、XML 映射规范

**1. ResultMap** — 每个实体定义一个 `<resultMap>`，复用 TypeHandler：

```xml
<resultMap id="externalEventResult" type="com.gien.gits.ontology.ExternalEvent">
    <id property="eventId" column="event_id"/>
    <result property="eventDate" column="event_date"/>
    <result property="sourceType" column="source_type"
            typeHandler="com.gien.gits.adapter.persistence.common.typehandler.EnumTypeHandler"/>
    <result property="linkedThemes" column="linked_themes"
            typeHandler="com.gien.gits.adapter.persistence.common.typehandler.StringListJsonTypeHandler"/>
    ...
</resultMap>
```

**2. SQL 语句** — 使用 `<sql>` 片段复用列列表：

```xml
<sql id="externalEventColumns">
    event_id, event_date, source_type, source_name, entity,
    title, content, confidence, reliability, bank_use_allowed,
    linked_themes, possible_business_signal, no_go_statement, evidence_ref
</sql>

<select id="findByEventId" resultMap="externalEventResult">
    SELECT <include refid="externalEventColumns"/>
    FROM external_event WHERE event_id = #{eventId}
</select>
```

**3. INSERT** — 使用 `useGeneratedKeys="false"`（业务 ID 由领域生成）：

```xml
<insert id="insert" parameterType="com.gien.gits.ontology.ExternalEvent">
    INSERT INTO external_event (<include refid="externalEventColumns"/>)
    VALUES (#{eventId}, #{eventDate}, #{sourceType, typeHandler=...}, ...)
</insert>
```

---

## 七、TypeHandler 规范

TypeHandler 属于跨子系统共享基础设施，统一放在 `common/typehandler/` 下。

针对本项目常见类型，统一实现以下 TypeHandler：

| TypeHandler | 处理类型 | 数据库列类型 | 说明 |
|-------------|----------|-------------|------|
| `EnumTypeHandler<E extends Enum<E>>` | 枚举 → `String` | `VARCHAR` | 存储 `enum.name()`，读取 `E.valueOf()` |
| `StringListJsonTypeHandler` | `List<String>` → JSON | `VARCHAR/TEXT` | 替代 `JsonHelper.toJsonArray()` |
| `InstantTypeHandler` | `Instant` → `Timestamp` | `TIMESTAMP` | 统一时间映射 |
| `UuidListJsonTypeHandler` | `List<UUID>` → JSON | `VARCHAR/TEXT` | 替代手写 UUID 列表序列化 |

注册方式：在 `application.yaml` 中配置包扫描路径，而非逐个 `@MappedJdbcTypes`：

```yaml
mybatis:
  type-handlers-package: com.gien.gits.adapter.persistence.common.typehandler
```

---

## 八、持久化服务实现规范

### Service 与 Port 的关系

```
modules/{domain}/port/WritableExternalEventRepository.java   ← Port 接口（已有）
        ↑ implements
adapters/persistence-relational/.../foundation/ontology/service/ExternalEventService.java  ← 持久化服务
        ↓ delegates to
adapters/persistence-relational/.../foundation/ontology/mapper/ExternalEventMapper.java    ← Mapper 接口
```

- **Port 接口** = 业务服务接口（已存在于 `modules/` 的 `port/` 包中）
- **Service** = 持久化服务实现（实现 Port，委托给 Mapper）
- 不再额外定义 Service 接口 + Impl 分层，Port 接口已承担此职责

### 示例实现

```java
package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ExternalEventMapper;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import java.util.List;
import java.util.Optional;

public class ExternalEventService implements WritableExternalEventRepository {

    private final ExternalEventMapper mapper;

    public ExternalEventService(ExternalEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ExternalEvent e) { mapper.insert(e); }

    @Override
    public Optional<ExternalEvent> findByEventId(String eventId) { return mapper.findByEventId(eventId); }

    @Override
    public List<ExternalEvent> findByEventType(String eventType) { return mapper.findBySourceType(eventType); }

    // ... 其他方法委托给 mapper
}
```

### Bean 注册

在 `RepositoryConfig` 中通过条件装配切换 JDBC / MyBatis 实现：

```java
@ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "mybatis", matchIfMissing = false)
@Bean
public WritableExternalEventRepository externalEventService(ExternalEventMapper mapper) {
    return new ExternalEventService(mapper);
}

@ConditionalOnProperty(name = "gits.persistence.mode", havingValue = "jdbc", matchIfMissing = true)
@Bean
public WritableExternalEventRepository jdbcExternalEventRepository(JdbcTemplate jdbc) {
    return new JdbcExternalEventRepository(jdbc);
}
```

---

## 九、配置规范

`application.yaml` 新增：

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.gien.gits.ontology,com.gien.gits.action.domain,com.gien.gits.engagement
  type-handlers-package: com.gien.gits.adapter.persistence.common.typehandler
  configuration:
    map-underscore-to-camel-case: true      # 列名 snake_case → camelCase
    default-enum-type-handler: com.gien.gits.adapter.persistence.common.typehandler.EnumTypeHandler
    jdbc-type-for-null: NULL                 # null 参数处理
    lazy-loading-enabled: false              # 禁用懒加载（本项目为简单查询）

gits:
  persistence:
    mode: jdbc    # jdbc | mybatis — 切换持久化实现
```

`@MapperScan` 配置类：

```java
@Configuration
@MapperScan({
    "com.gien.gits.adapter.persistence.foundation.ontology.mapper",
    "com.gien.gits.adapter.persistence.foundation.action.mapper",
    "com.gien.gits.adapter.persistence.foundation.engagement.mapper",
    "com.gien.gits.adapter.persistence.foundation.journey.mapper",
    "com.gien.gits.adapter.persistence.business.*.mapper"
})
public class MyBatisConfig {
}
```

> `@MapperScan` 显式列出基建层各模块的 mapper 包，业务层使用通配符 `business.*.mapper` 扫描。

---

## 十、基建模块与 Port 映射表

### foundation/ontology（运营本体）

| 领域模型 | Port 接口 | 现有 JDBC 实现 |
|---------|----------|---------------|
| ExternalEvent | WritableExternalEventRepository | JdbcExternalEventRepository |
| Customer | WritableCustomerRepository | JdbcCustomerRepository |
| Claim | WritableClaimRepository | JdbcClaimRepository |
| ClaimLifecycleEvent | WritableClaimLifecycleRepository | — |
| CreditFacility | WritableCreditFacilityRepository | JdbcCreditFacilityRepository |
| LegalEntity | WritableLegalEntityRepository | JdbcLegalEntityRepository |
| GroupRelationship | WritableGroupRelationshipRepository | JdbcGroupRelationshipRepository |
| KycGapProfile | WritableKycGapProfileRepository | JdbcKycGapProfileRepository |
| Interaction | WritableInteractionRepository | JdbcInteractionRepository |
| InteractionExtension | WritableInteractionExtensionRepository | — |
| Commitment | WritableCommitmentRepository | — |
| OperatingCase | WritableOperatingCaseRepository | JdbcOperatingCaseRepository |
| FactReconciliationCase | WritableFactReconciliationRepository | JdbcFactReconciliationRepository |
| OpportunitySignal | WritableOpportunitySignalRepository | JdbcOpportunitySignalRepository |
| BankRelationshipSnapshot | WritableBankRelationshipSnapshotRepository | JdbcBankRelationshipSnapshotRepository |
| Transaction | WritableTransactionRepository | JdbcTransactionRepository |
| TransactionRecord | WritableTransactionRecordRepository | JdbcTransactionRecordRepository |
| PolicyRule | WritablePolicyRuleRepository | JdbcPolicyRuleRepository |
| ProductKnowledgeVersion | WritableProductKnowledgeVersionRepository | JdbcProductKnowledgeVersionRepository |
| ProductCatalog | WritableProductCatalogRepository | JdbcProductCatalogRepository |
| RelationshipReport | WritableRelationshipReportRepository | JdbcRelationshipReportRepository |
| Opportunity | WritableOpportunityRepository | JdbcOpportunityRepository |
| EvidenceVersionLink | WritableEvidenceVersionLinkRepository | JdbcEvidenceVersionLinkRepository |
| ControlledAction | — | — |
| HumanConfirmation | — | — |
| ActionReceipt | — | — |

### foundation/action（人工行动）

| 领域模型 | Port 接口 | 现有 JDBC 实现 |
|---------|----------|---------------|
| Task | WritableTaskRepository | JdbcTaskRepository |
| RecordingConsent | WritableRecordingConsentRepository | — |

### foundation/engagement（客户触达场景）

| 领域模型 | Port 接口 | 现有 JDBC 实现 |
|---------|----------|---------------|
| PrevisitReportContent | WritablePrevisitReportContentRepository | JdbcPrevisitReportContentRepository |
| MeetingScript | WritableMeetingScriptRepository | — |
| OutreachScript | WritableOutreachScriptRepository | — |
| PostvisitAnalysisContent | WritablePostvisitAnalysisContentRepository | — |

### foundation/journey（经营旅程）

| 领域模型 | Port 接口 | 现有 JDBC 实现 |
|---------|----------|---------------|
| CustomerJourney | WritableCustomerJourneyRepository | — |

### business/（业务子系统，按需创建）

| 业务子系统 | 典型跨实体查询 | 创建时机 |
|-----------|-------------|---------|
| marketinsight | ExternalEvent + OpportunitySignal + Customer 联合 | 需要市场机会聚合看板时 |
| customer360 | Customer + CreditFacility + GroupRelationship 联合 | 需要客户全景视图时 |
| kycprep | KycGapProfile + Claim + PolicyRule 联合 | 需要 KYC 缺口分析报告时 |
| postvisit | PostvisitAnalysisContent + RelationshipReport + Opportunity 联合 | 需要访后综合报告时 |
| crmwriteback | ControlledAction + HumanConfirmation 联合 | 需要受控写回审批流时 |

---

## 十一、迁移策略

### 阶段 1：共存期（当前）

- 引入 MyBatis 依赖和基础设施
- 通过 `gits.persistence.mode` 配置切换
- 默认 `jdbc`，不破坏现有行为
- 新增仓储优先用 MyBatis 实现

### 阶段 2：按基建模块迁移

按基建模块整体迁移，每个模块内部按简单→复杂排序：

**第一批（foundation/ontology — 单表、字段少）**：
1. ExternalEvent（14字段）、OpportunitySignal
2. EvidenceVersionLink、Opportunity
3. BankRelationshipSnapshot、Transaction、TransactionRecord

**第二批（foundation/ontology — 单表、枚举多）**：
4. Customer（22字段）、CreditFacility、LegalEntity、GroupRelationship
5. Claim、ClaimLifecycleEvent、KycGapProfile
6. PolicyRule、ProductCatalog、ProductKnowledgeVersion

**第三批（foundation/ontology — 主表+子表）**：
7. Interaction + InteractionExtension、Commitment、OperatingCase
8. FactReconciliationCase、RelationshipReport
9. ControlledAction、HumanConfirmation、ActionReceipt

**第四批（foundation/engagement — 场景内容）**：
10. PrevisitReportContent、MeetingScript、OutreachScript、PostvisitAnalysisContent

**第五批（foundation/action + foundation/journey）**：
11. Task、RecordingConsent
12. CustomerJourney

**第六批（business/ — 业务组合查询，按需）**：
13. marketinsight、customer360 等业务子系统组合查询

每迁移一个实体，跑通全量测试后删除对应 `JdbcXxxRepository`。

### 阶段 3：清理期

- 移除 `JdbcTemplate` 依赖
- 移除 `JsonHelper`（被 TypeHandler 替代）
- 移除 `gits.persistence.mode` 切换逻辑
- 移除 `v11/` 包

---

## 十二、禁令

| # | 禁令 | 原因 |
|---|------|------|
| 1 | **禁止在 Mapper XML 中写业务逻辑** | MyBatis 只做映射，业务逻辑留在 Service/Domain |
| 2 | **禁止使用 MyBatis 动态 SQL 拼接业务规则** | 条件过滤在 Service 层完成，Mapper 只做固定查询 |
| 3 | **禁止引入 MyBatis-Plus** | 保持框架最小化，避免与六边形架构冲突 |
| 4 | **禁止 Mapper 接口返回非领域类型** | 不引入 PO/DTO 中间层，Mapper 直接映射到 `record` |
| 5 | **禁止在 XML 中硬编码表名/列名** | 使用 `<sql>` 片段统一管理，与 Flyway 迁移对齐 |
| 6 | **禁止跳过 TypeHandler 直接序列化 JSON** | 统一通过 TypeHandler 处理，不混用 `JsonHelper` |
| 7 | **禁止在迁移完成前删除 JDBC 实现** | 共存期两种实现必须同时可用 |
| 8 | **禁止跨基建模块直接引用 Mapper** | 模块间通过 Port 接口或 Service 解耦，Mapper 仅在本模块 Service 内使用 |
| 9 | **禁止业务层 Mapper 包含单实体 CRUD** | 单实体 CRUD 归属基建层，业务层仅做跨实体组合查询 |
| 10 | **禁止在基建层写业务组合查询** | 基建层只做单实体 CRUD，跨实体查询归业务层 |

---

## 十三、测试规范

| 层 | 测试方式 | 位置 |
|----|---------|------|
| Mapper | `@MybatisTest` + H2 | `persistence-relational` 模块 `test/`，按业务子系统组织 |
| Service | 单元测试 Mock Mapper | `persistence-relational` 模块 `test/`，按业务子系统组织 |
| 集成 | 现有 E2E 测试（切换 mode=mybatis 跑一遍） | `apps/api` 模块 `test/` |

测试包结构与源码一致：

```
adapters/persistence-relational/src/test/java/com/gien/gits/adapter/persistence/
├── marketinsight/
│   ├── mapper/
│   │   └── ExternalEventMapperTest.java
│   └── service/
│       └── ExternalEventServiceTest.java
├── customer360/
│   ├── mapper/
│   │   └── CustomerMapperTest.java
│   └── service/
│       └── CustomerServiceTest.java
└── ...
```
