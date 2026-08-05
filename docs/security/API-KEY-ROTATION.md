# API Key 轮转机制

> P16 G10: 安全加固 — API Key 轮转设计与操作手册

## 1. 概述

API Key 轮转机制支持多 Key 并行生效，确保在 Key 更换期间服务不中断。

## 2. 配置方式

### application.yaml

```yaml
engagement:
  security:
    # 主 Key — 当前生效的 API Key
    api-key: ${API_KEY_PRIMARY:}
    # 轮转 Key — 旧 Key 在轮转期间仍可使用
    api-key-rotation: ${API_KEY_ROTATION:}
    # 轮转截止时间 — ISO 8601 格式，过期后轮转 Key 失效
    api-key-rotation-deadline: ${API_KEY_ROTATION_DEADLINE:}
```

### 环境变量

```bash
export API_KEY_PRIMARY="new-key-2026-q3"
export API_KEY_ROTATION="old-key-2026-q2"
export API_KEY_ROTATION_DEADLINE="2026-09-01T00:00:00Z"
```

## 3. 轮转流程

### 步骤 1: 部署新 Key

1. 生成新 API Key (至少 32 字符，包含大小写字母、数字和特殊字符)
2. 设置环境变量:
   - `API_KEY_PRIMARY` = 新 Key
   - `API_KEY_ROTATION` = 旧 Key
   - `API_KEY_ROTATION_DEADLINE` = 轮转截止时间 (建议 7 天后)
3. 重启服务

### 步骤 2: 通知客户端迁移

1. 通知所有客户端在截止时间前切换到新 Key
2. 监控旧 Key 使用量

### 步骤 3: 清理旧 Key

1. 确认所有客户端已迁移
2. 移除 `API_KEY_ROTATION` 和 `API_KEY_ROTATION_DEADLINE`
3. 重启服务

## 4. 认证逻辑

```
请求到达
  ├─ 主 Key 为空? → 跳过认证 (开发模式)
  ├─ X-API-KEY == 主 Key? → 认证通过
  ├─ 轮转 Key 存在 且 未过期?
  │   ├─ X-API-KEY == 轮转 Key? → 认证通过 (记录警告)
  │   └─ 否 → 认证失败
  └─ 认证失败
```

## 5. Key 生成规范

- 长度: 至少 32 字符
- 字符集: 大写字母 + 小写字母 + 数字 + 特殊字符 (- _)
- 生成方式: `openssl rand -base64 32 | tr -d '/+=' | head -c 40`
- 存储方式: 环境变量或 Vault，禁止明文写入配置文件

## 6. 审计

所有 Key 认证事件（成功/失败）均通过 `AuditLogPort` 记录:
- 成功认证: `action=API_KEY_AUTH, outcome=SUCCESS`
- 轮转 Key 使用: `action=API_KEY_ROTATION_USED, outcome=SUCCESS`
- 认证失败: `action=API_KEY_AUTH, outcome=FAILURE`

## 7. 紧急轮转

如 Key 泄露，立即执行:

```bash
# 1. 生成新 Key
NEW_KEY=$(openssl rand -base64 32 | tr -d '/+=' | head -c 40)

# 2. 设置环境变量 (旧 Key 作为轮转 Key，短截止时间)
export API_KEY_PRIMARY="$NEW_KEY"
export API_KEY_ROTATION="$COMPROMISED_KEY"
export API_KEY_ROTATION_DEADLINE=$(date -u -d '+1 hour' +%Y-%m-%dT%H:%M:%SZ)

# 3. 重启服务
docker compose restart api
```
