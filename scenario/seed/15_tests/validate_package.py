from pathlib import Path
import csv, json, sys

ROOT=Path(__file__).resolve().parents[1]

def j(rel):
    return json.loads((ROOT/rel).read_text(encoding="utf-8"))
def jl(rel):
    return [json.loads(x) for x in (ROOT/rel).read_text(encoding="utf-8").splitlines() if x.strip()]
def c(rel):
    with (ROOT/rel).open(encoding="utf-8-sig") as f:
        return list(csv.DictReader(f))

errors=[]
coverage=j("00_governance/P0_COVERAGE_REGISTER.json")["coverage"]
if len(coverage)!=27 or any(x["coverage_status"]!="FULLY_BOUND" for x in coverage):
    errors.append("P0 coverage incomplete")

entities=c("02_master_data/legal_entities.csv")
if len({x["entity_id"] for x in entities})!=len(entities):
    errors.append("duplicate entity_id")

tx=c("03_bank_data/transactions_20260101_20260708.csv")
if len({x["transaction_id"] for x in tx})!=len(tx):
    errors.append("duplicate transaction_id")

balances=c("03_bank_data/daily_account_balances.csv")
if len({(x["date"],x["account_id"]) for x in balances})!=len(balances):
    errors.append("duplicate balance key")

equip=c("12_new_evidence/equipment_list_20260710.csv")
if abs(sum(float(x["amount_cny"]) for x in equip)-32800000)>0.01:
    errors.append("equipment total != 3280w")

pay=c("12_new_evidence/payment_schedule_20260710.csv")
if abs(sum(float(x["ratio"]) for x in pay)-1.0)>1e-9:
    errors.append("payment ratio != 1.0")
if abs(sum(float(x["amount_cny"]) for x in pay)-32800000)>0.01:
    errors.append("payment total != 3280w")

from datetime import date
def s(d1,d2):
    d1=date.fromisoformat(d1); d2=date.fromisoformat(d2)
    return sum(float(x["amount_cny"]) for x in tx if d1<=date.fromisoformat(x["date"])<=d2 and x["counterparty_type"]=="设备供应商" and x["direction"]=="OUT")
prior=s("2026-04-10","2026-05-24")
recent=s("2026-05-25","2026-07-08")
change=(recent/prior-1)*100
if not (31.0<=change<=33.0):
    errors.append(f"equipment payment change {change:.2f}% not in 31-33")

gates={x["gate_id"]:x for x in jl("10_human_gates/human_gate_decisions.jsonl")}
calls={x["call_id"]:x for x in jl("09_skill_agent/skill_invocation_trace.jsonl")}
tests={x["test_id"]:x for x in jl("15_tests/acceptance_tests.jsonl")}
for x in coverage:
    page=ROOT/"08_ui_states"/f"{x['page_state_ref']}.json"
    if not page.exists(): errors.append("missing page "+str(page))
    if x["human_gate_ref"] not in gates: errors.append("missing gate "+x["human_gate_ref"])
    if x["skill_call_ref"] not in calls: errors.append("missing call "+x["skill_call_ref"])
    if x["acceptance_test_ref"] not in tests: errors.append("missing test "+x["acceptance_test_ref"])

cmds=jl("11_outputs/CRM_WRITEBACK_COMMANDS.jsonl")
keys=[x["idempotency_key"] for x in cmds]
if len(keys)!=len(set(keys)): errors.append("duplicate idempotency_key")

versions=jl("05_knowledge/product_rule_versions.jsonl")
fal=[x for x in versions if x["product_id"]=="PROD-FAL"]
if not any(x["version"]=="2.2" and x["status"]=="ACTIVE" for x in fal): errors.append("PROD-FAL 2.2 not ACTIVE")
if not any(x["version"]=="2.1" and x["status"]=="RETIRED" for x in fal): errors.append("PROD-FAL 2.1 not RETIRED")

if gates.get("HG-E04",{}).get("decision")!="DECLINE_RECORDING":
    errors.append("recording consent gate incorrect")

gold=jl("06_interactions/meeting_utterances_gold.jsonl")
if any(x.get("production_input") for x in gold):
    errors.append("gold transcript marked production input")

print(json.dumps({
  "status":"PASS" if not errors else "FAIL",
  "errors":errors,
  "p0_count":len(coverage),
  "transactions":len(tx),
  "balances":len(balances),
  "equipment_payment_change_pct":round(change,2),
  "equipment_total":sum(float(x["amount_cny"]) for x in equip)
},ensure_ascii=False,indent=2))
sys.exit(1 if errors else 0)
