# P20 Dispatch｜Wiki-first与Ontology融合架构验证

```text
DISPATCH_ID=P20-WIKI-ONTOLOGY-FUSION
STATUS=PROPOSED_PENDING_OWNER_AUTHORIZATION
BASE_COMMIT=f8452638543da91b4e8f56c393ecadcd59e57364
LOOP=P20-wiki-ontology-fusion
CONTRACT_CANDIDATE_HOLDER=tech_lead
IMPLEMENTATION_ACTOR=PENDING_OWNER_ASSIGNMENT
QA_ACTOR=MUST_BE_INDEPENDENT
```

## 目标

以访前准备和3000万事实对账两个场景，验证Knowledge Map、Asset Manifest、Route Policy、Activation Contract、ActivationPlan、受控语义查询、ContextPackage、Skill和Agent闭环。

## Gap分解

| Gap | 范围 | 退出条件 |
|---|---|---|
| G0 | 合同和生成工具 | 6项Schema注册，generate/check通过 |
| G1 | 文件知识架构 | 4张地图、20项Manifest、2项Activation和5项Skill可校验 |
| G2 | 路由与计划 | 路由黄金用例通过，计划确定性和交叉引用通过 |
| G3 | 语义查询 | 注册CQ可执行，任意SPARQL被拒绝 |
| G4 | 上下文装配 | EvidenceBundle不再为空，权限和来源完整 |
| G5 | 两场景Shadow | 新旧链路并行，形成差异报告，不改变正式输出 |
| G6 | 回归与QA包 | No-Go、安全和原链路回归通过，交独立QA |

## 明确排除

- 真实RAG/OpenSPG/GraphDB/OpenMetadata；
- 全量业务模块迁移；
- 删除Legacy；
- 生产切换；
- 自动CRM写回；
- 开发自签QA。

## 开发规则

1. 先合同后实现；
2. `generated/`只读；
3. 失败先记录到`FAILURES.md`；
4. 每个Gap完成后提交明确证据；
5. 未经Owner批准不得从SHADOW切到FUSION；
6. 不得为了让测试通过而弱化默认拒绝、权限或No-Go规则。
7. 既有`loops/P19`保持原状态和Baton，P20不得复用其证据或签署。
