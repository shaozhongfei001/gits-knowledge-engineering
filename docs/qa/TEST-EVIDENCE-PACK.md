# 测试证据包说明

> 项目: GITS知识工程 | 版本: V1.0 | 日期: 2026-08-06

## 概述

测试证据包是QA独立验收的核心交付物，包含证明系统满足验收标准的全部客观证据。

## 证据包结构

```
test-evidence/
├── build/                          # 构建证据
│   ├── mvn-verify.log              # Maven构建日志
│   └── jacoco-report/              # JaCoCo覆盖率报告
├── unit-tests/                     # 单元测试证据
│   └── surefire-reports/           # Surefire测试报告
├── e2e/                            # E2E测试证据
│   ├── e2e-acceptance.log          # E2E验收脚本输出
│   └── prod-verify.log             # 生产验证脚本输出
├── performance/                    # 性能测试证据
│   └── k6-summary.json             # k6测试结果
├── security/                       # 安全测试证据
│   └── security-checklist.md       # 安全检查结果
└── regression-report.md            # 回归测试报告
```

## 证据采集方法

### 1. 构建证据
```bash
./mvnw verify -Ddependency-check.skip=true 2>&1 | tee test-evidence/build/mvn-verify.log
cp -r apps/api/target/site/jacoco test-evidence/build/jacoco-report/
```

### 2. 单元测试证据
```bash
find . -path "*/surefire-reports/TEST-*.xml" -exec cp {} test-evidence/unit-tests/surefire-reports/ \;
```

### 3. E2E测试证据
```bash
./scripts/e2e-acceptance.sh http://localhost:8080 $API_KEY 2>&1 | tee test-evidence/e2e/e2e-acceptance.log
./scripts/prod-verify.sh http://localhost:8080 2>&1 | tee test-evidence/e2e/prod-verify.log
```

### 4. 性能测试证据
```bash
k6 run --out json=test-evidence/performance/k6-summary.json scripts/k6/smoke.js
```

## 通过标准

| 证据类型 | 通过条件 |
|----------|----------|
| 构建 | BUILD SUCCESS, 0 Failures |
| 覆盖率 | JaCoCo ≥ 80% |
| E2E | 42项全部PASS或N/A |
| 性能 | P95满足阈值 |
| 安全 | 全部PASS |

## 保留期限

测试证据包保留至下一版本发布后30天。
