---
{"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP15","assetType":"PROCESS_TOOL","name":"SP-15产品适配与综合方案","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"specs/knowledge-architecture/skills/SP-15.json","authority":"REFERENCE","contentMode":"EXECUTE","sourceVersionPolicy":"SKILL_VERSION"},"governance":{"owner":"公司金融产品管理部","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE","REVIEW"]},"capabilities":["基于客户需要和约束生成产品候选组合","解释适配条件和材料缺口"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["contextPackageId","productIntent"],"maxContextTokens":0,"failurePolicy":"DEGRADE_WITH_DISCLOSURE"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-PRODUCT-CARDS"],"limitations":["产品候选不等于授信审批、定价或销售承诺"]}
---

# SP-15产品适配与综合方案

本体先执行对象、用途和规则约束，再激活相关产品知识卡形成候选方案。
