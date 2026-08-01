# P0整改与红测记录

## 已关闭的5项BLOCKER

| ID | 原问题 | 整改 | 开发自检 |
|---|---|---|---|
| B-01 | generate/check缺脚本仍返回0 | Makefile先校验脚本存在和可执行；合同编译器异常统一非零 | PASS |
| B-02 | 占位、dummy E2E、空证据仍PASS | JSON证据板、命令白名单、日志hash、holder与状态联检 | PASS |
| B-03 | bootstrap非幂等、冲突覆盖 | plan/apply、冲突拒绝、manifest、备份、noop幂等、rollback | PASS |
| B-04 | `.env`、SQLite、Oracle源码缺保护 | `.gitignore`、秘密扫描、权限检查、CI门禁 | PASS |
| B-05 | Oracle只读fail-open | 旧资产默认隔离；只读事务失败关闭连接并抛错 | PASS |

以上为开发自检，等待独立QA复跑和抽样。

## 实际红测

### RED-01｜前端类型工具不兼容

- 现象：TypeScript 7.0.2与`vue-tsc 3.3.9`执行时报`ERR_PACKAGE_PATH_NOT_EXPORTED`。
- 根因：选择了尚未被当前Vue类型工具链支持的TypeScript主版本。
- 修复：锁定TypeScript 5.9.3，补充Node 22类型，启用依赖声明文件隔离检查；重新生成lockfile。
- 复测：`npm run check`、`npm run test`、`npm run build`全部PASS。
- 附带改进：TDesign改为按组件引入，主JS从约1279KB降至约183KB。

### ENV-01｜当前生成环境缺Java开发工具链

- 现象：当前环境只有Java 17运行时，没有`javac`、Maven和Docker。
- 处理：`make bootstrap-check`保持失败关闭；未伪造后端编译结果。
- 关闭条件：在用户Ubuntu目标仓库安装Java 21和Maven 3.9+后执行`make backend-test`，保留完整日志，再由独立QA复跑。
