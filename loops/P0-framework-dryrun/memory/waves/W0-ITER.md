# W0｜Iteration log（append-only）

由 `scripts/record_gate.py` 追加实际attempt、命令、退出码、证据hash和下一动作。没有实际执行不得登记PASS。

## Attempt 1｜20260801T050236Z

- Gate: `framework_test`
- Command: `make framework-test`
- Exit: `0`
- Evidence: `loops/P0-framework-dryrun/evidence/framework_test-20260801T050236Z.log`
- SHA256: `07a43b9231aa80534bf3f009766def635737a37a2b5c76ef34cab0f97e4ab928`

## Attempt 1｜20260801T050237Z

- Gate: `tooling_test`
- Command: `make tooling-test`
- Exit: `0`
- Evidence: `loops/P0-framework-dryrun/evidence/tooling_test-20260801T050237Z.log`
- SHA256: `f1f26aa13ec2d09eaca2b2c03c6aee84efc42ef7f05110c8ec77e40c57ee12e8`

## Attempt 1｜20260801T050237Z

- Gate: `contract_generate`
- Command: `make generate`
- Exit: `0`
- Evidence: `loops/P0-framework-dryrun/evidence/contract_generate-20260801T050237Z.log`
- SHA256: `048ab7c55cfbb3c6707b7212288bd84d678861d4abb19a7eca16fbb7a71803b8`

## Attempt 1｜20260801T050237Z

- Gate: `contract_check`
- Command: `make check`
- Exit: `0`
- Evidence: `loops/P0-framework-dryrun/evidence/contract_check-20260801T050237Z.log`
- SHA256: `528448f1fea10f4108f5ba46244c8fde17ad7c64010a98d1abde8ef29d469da9`

## Attempt 1｜20260801T050237Z

- Gate: `security_check`
- Command: `make security-check`
- Exit: `0`
- Evidence: `loops/P0-framework-dryrun/evidence/security_check-20260801T050237Z.log`
- SHA256: `682141f4d96b10995c75401d7804e5a09cfe71aae85332fc41be658346e3b50f`
