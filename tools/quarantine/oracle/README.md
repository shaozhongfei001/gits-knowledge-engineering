# Oracle专项资产隔离区

旧框架中的Catalog与Reconciliation方法可作为Oracle映射Spike候选，但本工程包**不默认安装或运行原脚本**。

启用前必须同时满足：

1. Data Owner、DBA和安全Owner书面授权；
2. 脱敏或受控SIT环境；
3. 依赖锁、Oracle版本夹具、离线解析回归和独立QA；
4. `SET TRANSACTION READ ONLY`失败即关闭连接并终止；
5. 目录700、文件600，源码默认只保存哈希和行段；
6. Catalog输出仅是数据映射证据/候选语义，不得自动升级为业务主本体。

`readonly_guard.py`只提供最小强制只读控制及离线自测，不包含真实连接信息，也不授权访问任何数据库。
