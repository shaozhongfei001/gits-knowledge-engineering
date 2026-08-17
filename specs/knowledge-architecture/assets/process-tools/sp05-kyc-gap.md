---
{"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP05","assetType":"PROCESS_TOOL","name":"SP-05经营KYC Gap智能","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"specs/knowledge-architecture/skills/SP-05.json","authority":"REFERENCE","contentMode":"EXECUTE","sourceVersionPolicy":"SKILL_VERSION"},"governance":{"owner":"KYC知识Owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE"]},"capabilities":["比较应知对象与已知事实","输出Unknown、Conflict和问题计划"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["activationPlanId","customerId"],"maxContextTokens":0,"failurePolicy":"DEGRADE_WITH_DISCLOSURE"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-KYC-QUESTION-LIBRARY"],"limitations":["Gap结果必须保留对象和证据引用"]}
---

# SP-05经营KYC Gap智能

输出未知与冲突，不用模型猜测补齐缺失事实。
