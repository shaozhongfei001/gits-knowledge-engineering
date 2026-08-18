---
{"schemaVersion":"1.0.0","assetId":"ASSET-KNOW-CLAIM-RECONCILIATION","assetType":"KNOWLEDGE_RULE","name":"Claim事实对账DMN规则","domain":"KD-CORP-RM","version":"0.1.0","status":"VALIDATION","source":{"type":"DMN_CONTRACT","uri":"specs/rules/claim-reconciliation.dmn","authority":"AUTHORITATIVE","contentMode":"REASON","sourceVersionPolicy":"CONTRACT_VERSION"},"governance":{"owner":"rule_owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["REASON"]},"capabilities":["判断Claim状态和证据充分度","支持3000万表达多路径解释"],"activation":{"mode":"REASON","adapter":"dmn-claim-reconciliation-adapter","requiredParameters":["claimRef","evidenceBundleRef","permissionDecisionId"],"maxContextTokens":800,"failurePolicy":"FAIL_CLOSED"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-CUSTOMER-ONTOLOGY"],"limitations":["规则结果不是审批结论","高风险结果必须进入Human Gate"]}
---

# Claim事实对账规则

用于稳定执行Claim、Fact、Unknown和Conflict边界，不允许模型绕过规则直接升级状态。
