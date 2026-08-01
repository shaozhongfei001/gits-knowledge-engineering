# Repository agent instructions

- Read `AI_GUIDE.md`, `specs/BASELINE_INDEX.yaml`, the active dispatch, and the active loop before editing.
- Treat `specs/CONTRACT_INDEX.yaml` as the only contract registry. Contract source changes precede generated artifacts and implementation.
- Never manually edit `generated/`.
- Do not implement business behavior for M01-M22 unless the active dispatch traces it to an authorized requirement/design object.
- AI-generated facts remain candidate Claims/Proposals until the required human-control path completes.
- Do not enable quarantined Oracle or Ossie assets without an approved ADR, data-owner authorization, and a dedicated loop.
- Development roles may record `DEV_SELF_CHECK_PASS`; only an independent QA actor may record `QA_PASS`.
- Never use `git add .`. Record failures before fixing them.
