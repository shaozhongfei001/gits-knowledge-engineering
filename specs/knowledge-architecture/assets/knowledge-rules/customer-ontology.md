---
{"schemaVersion":"1.0.0","assetId":"ASSET-KNOW-CUSTOMER-ONTOLOGY","assetType":"KNOWLEDGE_RULE","name":"客户持续经营本体与约束","domain":"KD-CORP-RM","version":"0.1.0","status":"VALIDATION","source":{"type":"SEMANTIC_CONTRACT","uri":"specs/semantic/gits-core.owl.ttl","authority":"AUTHORITATIVE","contentMode":"REASON","sourceVersionPolicy":"CONTRACT_VERSION"},"governance":{"owner":"semantic_architecture_owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["QUERY","REASON"]},"capabilities":["解析客户、主体、关系、Claim、Evidence和Opportunity语义","执行受控关系查询与约束解释"],"activation":{"mode":"REASON","adapter":"semantic-jena-adapter","requiredParameters":["semanticQueryId","subjectRef","permissionDecisionId","asOf"],"maxContextTokens":2200,"failurePolicy":"FAIL_CLOSED"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true},"relatedAssets":["ASSET-KNOW-EVIDENCE-POLICY","ASSET-KNOW-CLAIM-RECONCILIATION"],"limitations":["禁止Agent执行任意SPARQL","仅允许注册Semantic Query ID"]}
---

# 客户持续经营本体与约束

作为Ontology-first路径的语义控制资产，不保存运营对象权威状态。
