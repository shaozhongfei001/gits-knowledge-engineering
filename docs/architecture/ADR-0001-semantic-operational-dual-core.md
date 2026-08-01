# ADR-0001｜可编译语义合同与运行本体双核

状态：`ACCEPTED_FOR_ENGINEERING_CANDIDATE`

## 决策

采用“设计时可编译语义合同＋运行时运营对象控制面＋可重建投影”。

- LinkML Profile、人工OWL/SKOS、SHACL和标准映射表达规范语义；
- 关系型运营对象库保存Case、Interaction、Claim、Evidence、HumanConfirmation、Action、Receipt和Evaluation的权威状态；
- Jena通过`SemanticRepositoryPort`加载语义包、执行Shape/CQ，不向Agent开放任意SPARQL；
- 搜索、向量、图、宽表按版本化投影重建，不承载权威责任链；
- 模型输出必须先成为Candidate Claim或Proposal。

## 后果

语义标准与业务事务不会被单一图数据库或Agent框架绑定，但必须维护多合同编译、版本兼容、双时间与投影重建测试。

## 待验证

官方LinkML/Jena编译、Oracle R2RML/Ontop CQ、Ossie往返语义损失、权限与受控写回、六阶段真实Agent E2E。
