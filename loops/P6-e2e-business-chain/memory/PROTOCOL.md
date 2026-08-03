# P6 PROTOCOL

## Baton Transfer

- Baton holder: `tech_lead`
- Transfer: commit evidence to `memory/handoffs/` before releasing baton.
- Acquire: update `.baton.lock` with new holder + timestamp.

## Evidence Discipline

- Every gate result logged in `EVIDENCE.json` and `EVIDENCE.md`.
- Failures recorded in `FAILURES.md` before fixing.
- Only `independent_qa` role may record `QA_PASS`.
