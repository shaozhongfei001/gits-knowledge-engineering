# P22 W0 迭代日志

## Attempt 1｜20260819T125027Z

- Gate: `contract_generate`
- Command: `make generate`
- Exit: `0`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/contract_generate-20260819T125027Z.log`
- SHA256: `048ab7c55cfbb3c6707b7212288bd84d678861d4abb19a7eca16fbb7a71803b8`

## Attempt 1｜20260819T125036Z

- Gate: `contract_check`
- Command: `make check`
- Exit: `0`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/contract_check-20260819T125036Z.log`
- SHA256: `f1df423faa531925d9e6e830512e239821a8a5b349b8edb9d0ba2e4f35c3995b`

## Attempt 1｜20260819T125043Z

- Gate: `knowledge_architecture_check`
- Command: `python3 scripts/validate_knowledge_architecture.py`
- Exit: `0`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/knowledge_architecture_check-20260819T125043Z.log`
- SHA256: `979d2f58502d48b7bf1f19b79f25aded0b98e4595ccc35369108442e3f87ac1a`

## Attempt 1｜20260819T125043Z

- Gate: `element_read_gate`
- Command: `mvn -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test`
- Exit: `1`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/element_read_gate-20260819T125043Z.log`
- SHA256: `d27fc406998f9747f85b8a55cbbdcee20fc6104724f2cc9776e838d912a7b2f1`

## Attempt 2｜20260819T125117Z

- Gate: `element_read_gate`
- Command: `./mvnw -pl modules/knowledge-architecture,adapters/knowledge-filesystem -am test`
- Exit: `0`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/element_read_gate-20260819T125117Z.log`
- SHA256: `6fdd5024626a6818a31ecca32a6906f6c27305ff99178280c7d68e99b7d39461`

## Attempt 1｜20260819T125200Z

- Gate: `llm_read_map_gate`
- Command: `./mvnw -pl apps/api -am test -Dtest='KnowledgeWiki*' -Dsurefire.failIfNoSpecifiedTests=false`
- Exit: `1`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/llm_read_map_gate-20260819T125200Z.log`
- SHA256: `3c4b1c4b235624b6f6dc033a262bc37e678c29021424fa205877dee1cdb8b8af`

## Attempt 2｜20260819T125300Z

- Gate: `llm_read_map_gate`
- Command: `./mvnw -pl apps/api -am test -Dtest='KnowledgeWiki*' -Dsurefire.failIfNoSpecifiedTests=false`
- Exit: `1`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/llm_read_map_gate-20260819T125300Z.log`
- SHA256: `bbda6a31aae8f45a8196714a45469c114209274a0aca391408c2531d2fb537cb`

## Attempt 3｜20260819T125508Z

- Gate: `llm_read_map_gate`
- Command: `./mvnw -pl apps/api -am test -Dtest='KnowledgeWiki*' -Dsurefire.failIfNoSpecifiedTests=false`
- Exit: `0`
- Evidence: `loops/P22-llm-wiki-knowledge-map/evidence/llm_read_map_gate-20260819T125508Z.log`
- SHA256: `3812202db45841196620559651b0fd2483d43581dfcfc24535ad9d5096c75946`
