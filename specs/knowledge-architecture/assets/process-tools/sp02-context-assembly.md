---
{"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP02","assetType":"PROCESS_TOOL","name":"SP-02客户上下文与主体装配","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"specs/knowledge-architecture/skills/SP-02.json","authority":"REFERENCE","contentMode":"EXECUTE","sourceVersionPolicy":"SKILL_VERSION"},"governance":{"owner":"客户经营Owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE"]},"capabilities":["装配客户、主体、关系和证据上下文"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["activationPlanId","permissionDecisionId"],"maxContextTokens":0,"failurePolicy":"FAIL_CLOSED"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-DATA-CUSTOMER-PROFILE","ASSET-KNOW-CUSTOMER-ONTOLOGY"],"limitations":["不得绕过ActivationPlan读取资产"]}
---

# SP-02客户上下文与主体装配

负责把已授权资产和受控语义查询结果装配为ContextPackage。
