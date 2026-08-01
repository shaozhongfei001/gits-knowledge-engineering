# ADR-0006｜Java 包名统一到 com.gien.gits

状态：`ACCEPTED`

## 背景

根 `pom.xml` 的 `groupId` 已由 owner 设为 `com.gien.gits`，`name` 设为 `GITS Knowledge Engineering`。Java 源码原使用 `package com.gientech.hzb.kno.*`。owner 批准趁骨架期完成包路径统一。

## 决策

将 Java 包路径由 `com.gientech.hzb.kno` 全量重命名为 `com.gien.gits`，同步更新：

- 全部 `src/main/java`、`src/test/java` 下的 `package` 与 `import`；
- `@SpringBootApplication` 扫描路径；
- 不手工改 `generated/`。

领域前缀与合同标识（`hzb:` / `HZB-KNO-*`）由 ADR-0008 另行统一为 `gits`。

## 后果

- 与旧包路径不兼容；构建与测试须在新包下全绿。
- 验证：`./mvnw test` PASS；提交 `5d2b5bb`。
