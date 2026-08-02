# W0｜Iteration log（append-only）

由 `scripts/record_gate.py` 追加实际attempt、命令、退出码、证据hash和下一动作。没有实际执行不得登记PASS。

## Attempt 1｜20260802T034706Z

- Gate: `contract_check`
- Command: `make generate && make check`
- Exit: `0`
- Evidence: `loops/P4-customer-semantic-formalization/evidence/contract_check-20260802T034706Z.log`
- SHA256: `bc2936cee23d0ebd15f39c3c70c0112a959db65bd29c9de10ed5dec0a8de423f`

## Attempt 1｜20260802T034707Z

- Gate: `build_test`
- Command: `./mvnw -q --batch-mode --no-transfer-progress test`
- Exit: `0`
- Evidence: `loops/P4-customer-semantic-formalization/evidence/build_test-20260802T034707Z.log`
- SHA256: `6c34ca3bf0316b1c8203086b7516ae32f90c97c093abf96961342f4e362bde1c`

## Attempt 1｜20260802T034715Z

- Gate: `security_check`
- Command: `make security-check`
- Exit: `0`
- Evidence: `loops/P4-customer-semantic-formalization/evidence/security_check-20260802T034715Z.log`
- SHA256: `682141f4d96b10995c75401d7804e5a09cfe71aae85332fc41be658346e3b50f`

## Attempt 1｜20260802T034715Z

- Gate: `e2e_mechanism`
- Command: `bash scripts/e2e/mechanism_e2e.sh`
- Exit: `0`
- Evidence: `loops/P4-customer-semantic-formalization/evidence/e2e_mechanism-20260802T034715Z.log`
- SHA256: `b4a45d1c0595aa9d83f48e1eb9a84421c95e9c3b387029ba006c5275506d02a2`
