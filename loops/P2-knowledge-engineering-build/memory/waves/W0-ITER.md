# W0｜Iteration log（append-only）

由 `scripts/record_gate.py` 追加实际attempt、命令、退出码、证据hash和下一动作。没有实际执行不得登记PASS。

## Attempt 1｜20260801T175531Z

- Gate: `contract_check`
- Command: `make check`
- Exit: `0`
- Evidence: `loops/P2-knowledge-engineering-build/evidence/contract_check-20260801T175531Z.log`
- SHA256: `528448f1fea10f4108f5ba46244c8fde17ad7c64010a98d1abde8ef29d469da9`

## Attempt 1｜20260801T175531Z

- Gate: `build_test`
- Command: `./mvnw -q --batch-mode --no-transfer-progress test`
- Exit: `0`
- Evidence: `loops/P2-knowledge-engineering-build/evidence/build_test-20260801T175531Z.log`
- SHA256: `027c3e2d0d782203f6db5ccf4950461afb07bb273e2f709e1360ab018f9931e7`

## Attempt 1｜20260801T175537Z

- Gate: `security_check`
- Command: `make security-check`
- Exit: `0`
- Evidence: `loops/P2-knowledge-engineering-build/evidence/security_check-20260801T175537Z.log`
- SHA256: `682141f4d96b10995c75401d7804e5a09cfe71aae85332fc41be658346e3b50f`

## Attempt 1｜20260801T180717Z

- Gate: `e2e_mechanism`
- Command: `bash scripts/e2e/mechanism_e2e.sh`
- Exit: `0`
- Evidence: `loops/P2-knowledge-engineering-build/evidence/e2e_mechanism-20260801T180717Z.log`
- SHA256: `0751cfb5b981a705f9e20c71e5bf092a33d24e610510021071003b78fc8ded21`

## Attempt 1｜20260801T180905Z

- Gate: `independent_qa`
- Command: `make verify`
- Exit: `0`
- Evidence: `loops/P2-knowledge-engineering-build/evidence/independent_qa-20260801T180905Z.log`
- SHA256: `807b7803e976bbc9e5d683d40844af74414893e88577f3c381522cab09e8570a`
