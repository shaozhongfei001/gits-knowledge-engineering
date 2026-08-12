# 开发入口

## 最短路径
```bash
cd <V1.1包目录>
python 15_tests/validate_package.py
python -m unittest discover -s 15_tests -p 'test_*.py'
```

Mock API：
```bash
python -m pip install -r 13_api_fixtures/requirements_mock.txt
bash 13_api_fixtures/run_mock_api.sh
```

然后前端按：
`PAGE-01 → PAGE-02 → PAGE-03 → ... → PAGE-16`
实现。

## Coding Agent入口
把 `16_dev_handoff/CODING_AGENT_MASTER_PROMPT.md` 完整给Cursor/Codex/CodeBuddy，并把本包路径替换进 `<SET_PACKAGE_ROOT>`。

## 第一阶段不要做
- 真实银行接口；
- 真实CRM写回；
- 自动外发客户消息；
- 自动审批或自动商机升级。

先把P0 27项静态/Mock E2E跑通，再接真实系统。
