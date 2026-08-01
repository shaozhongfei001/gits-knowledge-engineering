# ADR-0006｜Java 包名统一到 com.gien.gits

状态：`PROPOSED`（候选 Proposal，未批准，不可执行）

## 背景

根 `pom.xml` 的 `groupId` 已由 owner 设为 `com.gien.gits`，`name` 设为 `GITS Knowledge Engineering`，子模块 pom 的 parent 与内部依赖 groupId 已同步对齐。但全部 Java 源码仍使用 `package com.gientech.hzb.kno.*`，`@SpringBootApplication(scanBasePackages = "com.gientech.hzb.kno")` 也指向旧包路径。

Maven `groupId` 与 Java `package` 不强制一致，当前构建不受阻断；但品牌标识不一致，长期会带来混淆与归档/发布元数据错位。

## 候选决策

将 Java 包路径由 `com.gientech.hzb.kno` 全量重命名为 `com.gien.gits.kno`（或 owner 指定的目标路径），同步更新：

- 所有 `src/main/java`、`src/test/java` 下的 `package` 与 `import` 声明；
- `@SpringBootApplication(scanBasePackages = ...)` 字符串；
- 任何配置文件、模块描述、`module-info`、Spring Modulith 包命名约定中引用旧包路径的位置；
- `generated/` 不手工改，由合同源驱动重新生成后核对。

## 影响范围

- 涉及全部 `apps/`、`modules/`、`adapters/` 下的 Java 源码与测试；
- 属于结构性大改，必须独立 dispatch/loop 承载，单列 baseline commit、证据与回归；
- 不在 P0-framework-dryrun 范围内，不得借该 loop 落地。

## 待办（批准前）

1. owner 确认目标包路径（`com.gien.gits.kno` 或其它）；
2. 单独立项 dispatch 与 loop，明确回归边界与证据要求；
3. 执行后更新本 ADR 状态并补齐 Owner、日期、替代方案、后果与验证证据。

## 约束

- 本 ADR 仅为候选 Proposal，不构成正式事实；
- 在 owner 批准并开 loop 之前，任何人/AI 不得擅自重命名包路径。
