---
{"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP07","assetType":"PROCESS_TOOL","name":"SP-07一致性与证据充分度","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"specs/knowledge-architecture/skills/SP-07.json","authority":"REFERENCE","contentMode":"EXECUTE","sourceVersionPolicy":"SKILL_VERSION"},"governance":{"owner":"evidence_owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE","REVIEW"]},"capabilities":["检查Claim与事实一致性","判断证据充分度和冲突状态"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["evidenceBundleId","permissionDecisionId"],"maxContextTokens":0,"failurePolicy":"FAIL_CLOSED"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-EVIDENCE-POLICY","ASSET-KNOW-CLAIM-RECONCILIATION"],"limitations":["不得将模型置信度替代证据充分度"]}
---

# SP-07一致性与证据充分度

是Ontology-first事实对账的关键Skill，高风险结果必须进入Human Gate。
