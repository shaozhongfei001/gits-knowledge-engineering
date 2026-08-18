---
{"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP10","assetType":"PROCESS_TOOL","name":"SP-10经营报告与版本草拟","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"specs/knowledge-architecture/skills/SP-10.json","authority":"REFERENCE","contentMode":"EXECUTE","sourceVersionPolicy":"SKILL_VERSION"},"governance":{"owner":"客户经营Owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE"]},"capabilities":["根据ContextPackage生成访前和持续经营报告","保留版本与证据引用"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["contextPackageId","reportType"],"maxContextTokens":0,"failurePolicy":"DEGRADE_WITH_DISCLOSURE"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-VISIT-SOP"],"limitations":["只能草拟","对外发送和正式写回必须人工确认"]}
---

# SP-10经营报告与版本草拟

Wiki-first负责报告结构和表达，本体提供已验证事实、关系和约束结果。
