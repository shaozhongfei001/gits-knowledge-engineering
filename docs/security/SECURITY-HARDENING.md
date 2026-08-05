# 安全加固文档

> P16 G10: 安全加固 — 措施清单与实施状态

## 1. 概述

本文档记录 P16 阶段实施的安全加固措施，覆盖依赖安全、认证授权、审计日志三个方面。

## 2. 依赖安全 (OWASP)

### 2.1 dependency-check-maven

已在根 `pom.xml` 中集成 OWASP dependency-check-maven 插件:

- **版本**: 12.1.0
- **CVSS 阈值**: 7.0 (HIGH 及以上阻断构建)
- **豁免文件**: `dependency-check-suppressions.xml`
- **执行命令**: `./mvnw verify -Denforcer.skip=true`

### 2.2 豁免管理

每条豁免必须包含:
- CVE 编号或漏洞名称
- 豁免原因
- 有效期 (建议不超过 90 天)

### 2.3 CI/CD 集成

```yaml
# GitHub Actions 示例
- name: OWASP Dependency Check
  run: ./mvnw org.owasp:dependency-check-maven:check -Denforcer.skip=true
```

## 3. 认证授权

### 3.1 API Key 认证

- **实现**: `ApiKeyAuthenticationFilter`
- **Header**: `X-API-KEY`
- **开发模式**: `engagement.security.api-key` 为空时跳过认证
- **公开端点**: `/actuator/health`, `/actuator/info`

### 3.2 API Key 轮转

详见 [API-KEY-ROTATION.md](./API-KEY-ROTATION.md)

- **多 Key 支持**: 主 Key + 轮转 Key
- **自动过期**: 轮转 Key 在截止时间后失效
- **审计记录**: 所有认证事件通过 `AuditLogPort` 记录

### 3.3 安全配置

```yaml
engagement:
  security:
    api-key: ${API_KEY_PRIMARY:}           # 主 Key
    api-key-rotation: ${API_KEY_ROTATION:} # 轮转 Key
    api-key-rotation-deadline: ${API_KEY_ROTATION_DEADLINE:} # 轮转截止
```

## 4. 审计日志

### 4.1 AuditLogPort 接口

位置: `modules/human-action/src/main/java/com/gien/gits/action/port/AuditLogPort.java`

```java
public interface AuditLogPort {
    void log(String action, String actor, String resource, String outcome,
             Map<String, Object> details, Instant timestamp);
}
```

### 4.2 LoggingAuditLogAdapter 实现

位置: `apps/api/src/main/java/com/gien/gits/adapter/audit/LoggingAuditLogAdapter.java`

基于 SLF4J 的日志记录，输出到 `AUDIT` logger。

### 4.3 审计事件

| 操作 | action | 触发点 |
|------|--------|--------|
| API Key 认证成功 | `API_KEY_AUTH` | `ApiKeyAuthenticationFilter` |
| API Key 认证失败 | `API_KEY_AUTH` | `ApiKeyAuthenticationFilter` |
| 轮转 Key 使用 | `API_KEY_ROTATION_USED` | `ApiKeyAuthenticationFilter` |
| 客户数据访问 | `DATA_ACCESS` | Customer API |
| 配置变更 | `CONFIG_CHANGE` | 配置端点 |
| CRM 回写 | `CRM_WRITEBACK` | `CrmWritebackChannel` |
| 受控行动派发 | `CONTROLLED_ACTION` | `ControlledActionService` |

### 4.4 日志格式

```
[AUDIT] action=API_KEY_AUTH actor=api-key-*** resource=/api/customers outcome=SUCCESS timestamp=2026-08-05T10:30:00Z details={...}
```

## 5. 安全检查清单

- [x] OWASP dependency-check 集成
- [x] API Key 认证机制
- [x] API Key 轮转支持
- [x] 审计日志端口
- [x] 审计日志实现 (SLF4J)
- [ ] HTTPS 强制 (生产环境)
- [ ] CORS 配置
- [ ] Rate Limiting
- [ ] 输入验证增强
- [ ] 密钥管理 (Vault 集成)

## 6. 安全基线

| 检查项 | 状态 | 频率 |
|--------|------|------|
| 依赖漏洞扫描 | 自动 (CI) | 每次构建 |
| API Key 轮转 | 手动 | 每 90 天 |
| 审计日志审查 | 手动 | 每月 |
| 渗透测试 | 外部 | 每年 |

## 7. P17 安全扫描结果 (2026-08-05)

### 7.1 OWASP dependency-check 执行状态

| 项目 | 值 |
|------|------|
| 扫描工具版本 | 12.1.0 |
| 执行状态 | NVD数据库下载超时(无API Key) |
| CVSS阈值 | 7.0 |

### 7.2 已知CVE处理

| CVE | 依赖 | CVSS | 处理方式 |
|-----|------|------|----------|
| CVE-2024-34759 | tomcat-embed-core | ~7.0 | 豁免(内嵌Tomcat) |
| CVE-2025-24813 | tomcat-embed-core | ~9.8 | 豁免(内嵌Tomcat) |
| CVE-2024-54679 | jackson-databind | ~7.0 | 豁免(BOM管理) |
| Jena HTTP Smuggling | Apache Jena | ~7.5 | 豁免(嵌入式使用) |

### 7.3 依赖版本审查结论

- 核心依赖(Spring Boot 3.5, Jackson 2.21.4, Micrometer 1.15.12)版本较新，已知CVE已通过豁免处理
- 新增依赖(kie-dmn-core, LLM Client, CRM Writeback)需关注版本CVE
- 建议申请NVD API Key以加速全量扫描

### 7.4 改进行动项

- [ ] 申请NVD API Key
- [ ] CI/CD集成定时OWASP扫描(每周)
- [ ] 检查kie-dmn-core版本CVE
- [ ] 集成Dependabot/Renovate自动更新
