# W0｜Iteration log（append-only）

由 `scripts/record_gate.py` 追加实际attempt、命令、退出码、证据hash和下一动作。没有实际执行不得登记PASS。

## Attempt 1｜20260801T183637Z

- Gate: `contract_check`
- Command: `make check`
- Exit: `0`
- Evidence: `loops/P3-mapping-and-gaps/evidence/contract_check-20260801T183637Z.log`
- SHA256: `97631b59ca51e0e88c5f0c97f4b82261a95b1cb9e01aa9a4f6341e142400fa85`

## Attempt 1｜20260801T183637Z

- Gate: `build_test`
- Command: `./mvnw -q --batch-mode --no-transfer-progress test`
- Exit: `0`
- Evidence: `loops/P3-mapping-and-gaps/evidence/build_test-20260801T183637Z.log`
- SHA256: `fd5f25af8db49984c01f04c1c895d607d2313e9d86368acbfc79abce312027bf`

## Attempt 1｜20260801T183645Z

- Gate: `security_check`
- Command: `make security-check`
- Exit: `0`
- Evidence: `loops/P3-mapping-and-gaps/evidence/security_check-20260801T183645Z.log`
- SHA256: `682141f4d96b10995c75401d7804e5a09cfe71aae85332fc41be658346e3b50f`

## Attempt 1｜20260801T183645Z

- Gate: `e2e_mechanism`
- Command: `bash scripts/e2e/mechanism_e2e.sh`
- Exit: `0`
- Evidence: `loops/P3-mapping-and-gaps/evidence/e2e_mechanism-20260801T183645Z.log`
- SHA256: `e3161ddf3adf70aa4e345d2dd13e5228f783b23f445480ed5f3f948861736646`

## Attempt 1｜20260801T183800Z

- Gate: `independent_qa`
- Command: `make verify`
- Exit: `0`
- Evidence: `loops/P3-mapping-and-gaps/evidence/independent_qa-20260801T183800Z.log`
- SHA256: `46467a14febd8206c1ae7efe9cdc58dd3067f5046c0aff655eb14ea6722721f7`
