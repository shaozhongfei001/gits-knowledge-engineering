from pathlib import Path
import csv, json
from fastapi import FastAPI, Body
from fastapi.responses import JSONResponse

ROOT = Path(__file__).resolve().parents[1]
app = FastAPI(title="Corporate RM Continuous Engagement Demo API", version="1.1.0")

def load_json(rel):
    return json.loads((ROOT / rel).read_text(encoding="utf-8"))

def load_jsonl(rel):
    return [json.loads(x) for x in (ROOT / rel).read_text(encoding="utf-8").splitlines() if x.strip()]

def load_csv(rel):
    with (ROOT / rel).open(encoding="utf-8-sig") as f:
        return list(csv.DictReader(f))

@app.get("/api/v1/rm/day")
def rm_day(rm_id: str = "P-RM-001", date: str = "2026-07-08"):
    return {"status":"OK","rm_id":rm_id,"date":date,"items":load_csv("02_master_data/rm_day_queue.csv")}

@app.get("/api/v1/customers/{customer_id}/operating-view")
def cov(customer_id: str, as_of: str = "2026-07-08"):
    return {"status":"OK","customer_id":customer_id,"as_of":as_of,"page_state":load_json("08_ui_states/PAGE-03.json")}

@app.get("/api/v1/customers/{customer_id}/kyc-gap")
def kyc(customer_id: str, visit_id: str = "VIS-20260708"):
    return {"status":"OK","customer_id":customer_id,"visit_id":visit_id,"page_state":load_json("08_ui_states/PAGE-06.json")}

@app.post("/api/v1/visits/prep")
def prep(body: dict = Body(...)):
    return {"status":"DRAFT_READY","request":body,"r1_path":"11_outputs/R1_PRE_VISIT_REPORT.md","r2_path":"11_outputs/R2_60SEC_BATTLE_CARD.md","human_gate":"HG-C01"}

@app.post("/api/v1/outreach/draft")
def outreach(body: dict = Body(...)):
    return {"status":"DRAFT_ONLY","request":body,"draft":load_json("11_outputs/R3_OUTREACH_PLAN.json"),"human_confirm_required":True}

@app.post("/api/v1/interactions/extract")
def extract(body: dict = Body(...)):
    return {"status":"OBJECTS_DRAFTED","request":body,"objects":load_jsonl("06_interactions/interaction_objects_20260708.jsonl"),"human_gate":"HG-F01"}

@app.post("/api/v1/reconciliation")
def reconcile(body: dict = Body(...)):
    return {"status":"PENDING_MULTI_PATH","request":body,"assessments":load_jsonl("06_interactions/claim_assessments.jsonl"),"human_gate":"HG-E01"}

@app.post("/api/v1/commitments/exit-confirm")
def exit_confirm(body: dict = Body(...)):
    return {"status":"DRAFT_READY","request":body,"commitments":load_jsonl("06_interactions/commitments.jsonl"),"human_gate":"HG-E03"}

@app.post("/api/v1/reports/post-visit")
def post_visit(body: dict = Body(...)):
    return {"status":"DRAFT_READY","request":body,"outputs":["R4","R5A","R5B"]}

@app.post("/api/v1/writeback/commands")
def writeback(body: dict = Body(...)):
    return {"status":"PROPOSE_ONLY","request":body,"commands":load_jsonl("11_outputs/CRM_WRITEBACK_COMMANDS.jsonl"),"human_gate":"HG-F05"}

@app.post("/api/v1/human-gates/{gate_id}/decision")
def human_gate(gate_id: str, body: dict = Body(...)):
    return {"status":"RECORDED","gate_id":gate_id,"decision":body,"side_effect_executed":False}

@app.post("/api/v1/evidence/ingest")
def evidence(body: dict = Body(...)):
    return {"status":"INGESTED_AS_DRAFT_EVIDENCE","request":body,"new_version_required":True,"human_gate":"HG-F06"}

@app.get("/api/v1/reports/r7-r8")
def r7r8(customer_id: str = "CUST-001"):
    return {"status":"OK","customer_id":customer_id,"r7":"11_outputs/R7_UPDATED_RELATIONSHIP_REPORT.md","r8":"11_outputs/R8_NEXT_PRE_VISIT_REPORT.md"}
