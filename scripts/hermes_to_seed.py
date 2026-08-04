#!/usr/bin/env python3
"""
Hermes演示数据 -> GITS知识工程种子数据转换脚本

将Hermes演示数据包中的"华东精工客户经理持续经营闭环场景"数据
转化为GITS知识工程的领域模型种子数据。

映射关系：
- 03_CUSTOMER_MASTER.json -> OperatingCase.subject
- 05_BANK_RELATIONSHIP_SNAPSHOT.csv -> OperatingCase.evidence.bankRelationship
- 07_CREDIT_FACILITY.json -> OperatingCase.evidence.creditFacility
- 04_GROUP_RELATIONSHIP.json -> OperatingCase.evidence.groupRelationship
- 14_CUSTOMER_OPERATING_VIEW_SNAPSHOT.json -> OperatingCase.evidence.operatingView
- 11_HISTORICAL_INTERACTIONS.jsonl -> Interaction + Claim(PROPOSAL)
- 10_EXTERNAL_EVENTS.jsonl -> Interaction + Claim(SYSTEM_FACT)
- 12_TIMELINE.csv -> CustomerJourney
- 15_KYC_GAP_PROFILE.json -> Claim(RISK_SIGNAL)
- 23_FACT_RECONCILIATION_CASE.json -> Claim(SYSTEM_FACT)
"""

import json
import csv
import uuid
import os
from datetime import datetime
from typing import Any


def to_uuid(text: str) -> str:
    """将文本ID转化为确定性UUID5。"""
    return str(uuid.uuid5(uuid.NAMESPACE_DNS, f"gits:hermes:{text}"))


def now_iso() -> str:
    return datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")


def load_json(path: str) -> Any:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def load_jsonl(path: str) -> list[dict]:
    records = []
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                records.append(json.loads(line))
    return records


def load_csv(path: str) -> list[dict]:
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        return list(reader)


def build_operating_case(customer_data: dict, credit: dict,
                         group: dict, operating_view: dict) -> dict[str, Any]:
    """从Hermes数据构建OperatingCase。"""
    cust = customer_data.get("customer", {})
    cust_id = cust.get("customer_id", "unknown")
    case_id = to_uuid(f"case:{cust_id}")

    # 构建subject
    subject = {
        "customerId": cust.get("customer_id"),
        "customerName": cust.get("customer_name"),
        "customerShortName": cust.get("customer_short_name"),
        "industry": cust.get("industry"),
        "customerTier": cust.get("customer_tier"),
        "enterpriseScale": cust.get("enterprise_scale"),
        "region": cust.get("region"),
        "riskLevel": cust.get("risk_level"),
        "relationshipSince": cust.get("relationship_since"),
        "rmId": cust.get("rm_id"),
        "rmName": cust.get("rm_name"),
        "managingBranch": cust.get("managing_branch"),
        "groupFlag": cust.get("group_flag"),
        "mainProducts": cust.get("main_products"),
        "coreTags": cust.get("core_tags"),
        "relationshipSummary": cust.get("relationship_summary"),
    }

    # 构建evidence
    evidence = {
        "creditFacility": credit,
        "groupRelationship": group,
        "operatingView": operating_view,
        "sourceSystem": "HERMES_DEMO",
        "sourceTables": [
            "03_CUSTOMER_MASTER.json",
            "07_CREDIT_FACILITY.json",
            "04_GROUP_RELATIONSHIP.json",
            "14_CUSTOMER_OPERATING_VIEW_SNAPSHOT.json",
        ],
    }

    case = {
        "caseId": case_id,
        "caseType": "CUSTOMER_OPERATING",
        "status": "ACTIVE",
        "subject": subject,
        "evidence": evidence,
        "recordedAt": now_iso(),
        "lastUpdated": customer_data.get("scenario", {}).get("generated_at", now_iso()),
    }

    return case


def build_interactions(historical: list[dict], external: list[dict]) -> list[dict[str, Any]]:
    """从Hermes数据构建Interaction。"""
    interactions = []

    # 历史交互
    for h in historical:
        interaction = {
            "interactionId": to_uuid(f"interaction:{h.get('interaction_id', 'unknown')}"),
            "timestamp": h.get("date"),
            "type": h.get("channel"),
            "channel": h.get("channel"),
            "participants": h.get("participants"),
            "content": {
                "rawSummary": h.get("raw_summary"),
                "extractedObjects": h.get("extracted_objects"),
            },
            "evidenceRef": h.get("evidence_ref"),
            "sourceSystem": "HERMES_DEMO",
            "sourceTable": "11_HISTORICAL_INTERACTIONS.jsonl",
        }
        interactions.append(interaction)

    # 外部事件
    for e in external:
        interaction = {
            "interactionId": to_uuid(f"event:{e.get('event_id', 'unknown')}"),
            "timestamp": e.get("event_date"),
            "type": "EXTERNAL_EVENT",
            "channel": e.get("source_type"),
            "content": {
                "title": e.get("title"),
                "content": e.get("content"),
                "entity": e.get("entity"),
                "confidence": e.get("confidence"),
                "possibleBusinessSignal": e.get("possible_business_signal"),
                "noGoStatement": e.get("no_go_statement"),
            },
            "linkedTheme": e.get("linked_theme"),
            "evidenceRef": e.get("evidence_ref"),
            "sourceSystem": "HERMES_DEMO",
            "sourceTable": "10_EXTERNAL_EVENTS.jsonl",
        }
        interactions.append(interaction)

    return interactions


def build_customer_journey(timeline: list[dict]) -> list[dict[str, Any]]:
    """从Hermes时间线构建CustomerJourney。"""
    journeys = []

    for t in timeline:
        journey = {
            "journeyId": to_uuid(f"journey:{t.get('date', '')}:{t.get('event_type', '')}"),
            "timestamp": t.get("date"),
            "phase": t.get("event_type"),
            "milestone": t.get("event"),
            "sourceRef": t.get("source_ref"),
            "status": "COMPLETED",
            "sourceSystem": "HERMES_DEMO",
            "sourceTable": "12_TIMELINE.csv",
        }
        journeys.append(journey)

    return journeys


def build_claims_from_interactions(historical: list[dict]) -> list[dict[str, Any]]:
    """从历史交互的extracted_objects构建Claim。"""
    claims = []

    for h in historical:
        for obj in h.get("extracted_objects", []):
            claim_type = "PROPOSAL" if obj.get("type") == "CLAIM" else "SYSTEM_FACT"
            claim = {
                "claimId": to_uuid(f"claim:{h.get('interaction_id', '')}:{obj.get('content', '')}"),
                "claimType": claim_type,
                "status": obj.get("status", "PENDING"),
                "statement": obj.get("content"),
                "recordedAt": now_iso(),
                "evidence": {
                    "sourceSystem": "HERMES_DEMO",
                    "sourceTable": "11_HISTORICAL_INTERACTIONS.jsonl",
                    "interactionId": h.get("interaction_id"),
                    "objectType": obj.get("type"),
                    "dueDate": obj.get("due_date"),
                },
            }
            claims.append(claim)

    return claims


def build_claims_from_external_events(external: list[dict]) -> list[dict[str, Any]]:
    """从外部事件构建Claim。"""
    claims = []

    for e in external:
        claim = {
            "claimId": to_uuid(f"event_claim:{e.get('event_id', 'unknown')}"),
            "claimType": "SYSTEM_FACT",
            "status": "VERIFIED_FACT" if e.get("confidence") == "HIGH" else "PROPOSED",
            "statement": f"[{e.get('source_type', '?')}] {e.get('title', '')}: {e.get('content', '')}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "10_EXTERNAL_EVENTS.jsonl",
                "eventId": e.get("event_id"),
                "eventType": e.get("source_type"),
                "entity": e.get("entity"),
                "confidence": e.get("confidence"),
                "reliability": e.get("reliability"),
                "possibleBusinessSignal": e.get("possible_business_signal"),
                "noGoStatement": e.get("no_go_statement"),
            },
        }
        claims.append(claim)

    return claims


def build_risk_signals_from_kyc(kyc_data: dict) -> list[dict[str, Any]]:
    """从KYC缺口构建RISK_SIGNAL Claim。"""
    claims = []
    cust_id = kyc_data.get("customer_id", "unknown")

    # partial_known -> RISK_SIGNAL
    for item in kyc_data.get("partial_known", []):
        claim = {
            "claimId": to_uuid(f"risk:kyc:partial:{item}"),
            "claimType": "RISK_SIGNAL",
            "status": "PROPOSED",
            "statement": f"KYC部分已知风险: {item}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "15_KYC_GAP_PROFILE.json",
                "customerId": cust_id,
                "category": "PARTIAL_KNOWN",
                "description": item,
            },
        }
        claims.append(claim)

    # stale -> RISK_SIGNAL
    for item in kyc_data.get("stale", []):
        claim = {
            "claimId": to_uuid(f"risk:kyc:stale:{item}"),
            "claimType": "RISK_SIGNAL",
            "status": "PROPOSED",
            "statement": f"KYC过期信息风险: {item}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "15_KYC_GAP_PROFILE.json",
                "customerId": cust_id,
                "category": "STALE",
                "description": item,
            },
        }
        claims.append(claim)

    # conflicting_or_ambiguous -> RISK_SIGNAL
    for item in kyc_data.get("conflicting_or_ambiguous", []):
        claim = {
            "claimId": to_uuid(f"risk:kyc:conflict:{item}"),
            "claimType": "RISK_SIGNAL",
            "status": "PROPOSED",
            "statement": f"KYC冲突/模糊信息风险: {item}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "15_KYC_GAP_PROFILE.json",
                "customerId": cust_id,
                "category": "CONFLICTING",
                "description": item,
            },
        }
        claims.append(claim)

    # unknown -> RISK_SIGNAL
    for item in kyc_data.get("unknown", []):
        claim = {
            "claimId": to_uuid(f"risk:kyc:unknown:{item}"),
            "claimType": "RISK_SIGNAL",
            "status": "PROPOSED",
            "statement": f"KYC未知信息风险: {item}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "15_KYC_GAP_PROFILE.json",
                "customerId": cust_id,
                "category": "UNKNOWN",
                "description": item,
            },
        }
        claims.append(claim)

    return claims


def build_claims_from_reconciliation(rec_data: dict) -> list[dict[str, Any]]:
    """从事实对账构建Claim。"""
    claims = []

    for rec in rec_data.get("cases", []):
        structured = rec.get("structured_fact", {})
        interaction = rec.get("interaction_claim", {})

        claim = {
            "claimId": to_uuid(f"rec:{rec.get('reconciliation_id', 'unknown')}"),
            "claimType": "SYSTEM_FACT",
            "status": "PROPOSED",
            "statement": f"事实对账[{rec.get('topic', '?')}]: {structured.get('content', '')} vs {interaction.get('content', '')}",
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "HERMES_DEMO",
                "sourceTable": "23_FACT_RECONCILIATION_CASE.json",
                "reconciliationId": rec.get("reconciliation_id"),
                "topic": rec.get("topic"),
                "structuredFact": structured,
                "interactionClaim": interaction,
                "ontologyDistinction": rec.get("ontology_distinction"),
            },
        }
        claims.append(claim)

    return claims


def main():
    data_dir = "/home/szf/dev/data/Hermes演示数据包_V1.0"
    output_dir = "/home/szf/dev/gits-knowledge-engineering/specs/data"

    # 加载所有数据源
    print("加载Hermes演示数据...")
    customer_data = load_json(os.path.join(data_dir, "03_CUSTOMER_MASTER.json"))
    credit = load_json(os.path.join(data_dir, "07_CREDIT_FACILITY.json"))
    group = load_json(os.path.join(data_dir, "04_GROUP_RELATIONSHIP.json"))
    operating_view = load_json(os.path.join(data_dir, "14_CUSTOMER_OPERATING_VIEW_SNAPSHOT.json"))
    historical = load_jsonl(os.path.join(data_dir, "11_HISTORICAL_INTERACTIONS.jsonl"))
    external = load_jsonl(os.path.join(data_dir, "10_EXTERNAL_EVENTS.jsonl"))
    timeline = load_csv(os.path.join(data_dir, "12_TIMELINE.csv"))
    kyc_gaps = load_json(os.path.join(data_dir, "15_KYC_GAP_PROFILE.json"))
    reconciliation = load_json(os.path.join(data_dir, "23_FACT_RECONCILIATION_CASE.json"))

    print(f"  客户主数据: 1个")
    print(f"  授信额度: 1个")
    print(f"  集团关系: {len(group.get('entities', []))}个实体")
    print(f"  历史交互: {len(historical)}条")
    print(f"  外部事件: {len(external)}条")
    print(f"  时间线: {len(timeline)}条")
    print(f"  KYC缺口: {len(kyc_gaps.get('partial_known', []))} partial + {len(kyc_gaps.get('stale', []))} stale + {len(kyc_gaps.get('conflicting_or_ambiguous', []))} conflict + {len(kyc_gaps.get('unknown', []))} unknown")
    print(f"  事实对账: {len(reconciliation.get('cases', []))}条")

    # 构建领域对象
    print("\n构建领域对象...")

    print("构建经营案例...")
    case = build_operating_case(customer_data, credit, group, operating_view)
    print(f"  构建 1 个经营案例")

    print("构建交互记录...")
    interactions = build_interactions(historical, external)
    print(f"  构建 {len(interactions)} 条交互记录")

    print("构建客户旅程...")
    journeys = build_customer_journey(timeline)
    print(f"  构建 {len(journeys)} 条客户旅程")

    print("从交互构建Claim...")
    interaction_claims = build_claims_from_interactions(historical)
    print(f"  构建 {len(interaction_claims)} 条交互Claim")

    print("从外部事件构建Claim...")
    event_claims = build_claims_from_external_events(external)
    print(f"  构建 {len(event_claims)} 条事件Claim")

    print("从KYC缺口构建风险信号...")
    risk_signals = build_risk_signals_from_kyc(kyc_gaps)
    print(f"  构建 {len(risk_signals)} 条风险信号")

    print("从事实对账构建Claim...")
    rec_claims = build_claims_from_reconciliation(reconciliation)
    print(f"  构建 {len(rec_claims)} 条对账Claim")

    # 组装输出数据
    all_claims = interaction_claims + event_claims + risk_signals + rec_claims

    output_data = {
        "version": "0.1.0",
        "generatedAt": now_iso(),
        "source": "HERMES_DEMO_DATA",
        "sourceDirectory": data_dir,
        "scenario": customer_data.get("scenario", {}),
        "summary": {
            "totalOperatingCases": 1,
            "totalInteractions": len(interactions),
            "totalCustomerJourneys": len(journeys),
            "totalClaims": len(all_claims),
            "claimBreakdown": {
                "interactionClaims": len(interaction_claims),
                "eventClaims": len(event_claims),
                "riskSignals": len(risk_signals),
                "reconciliationClaims": len(rec_claims),
            },
        },
        "operatingCases": [case],
        "interactions": interactions,
        "customerJourneys": journeys,
        "claims": all_claims,
    }

    output_path = f"{output_dir}/hermes-seed-data.v0.1.json"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"\n种子数据已写入: {output_path}")
    print(f"总计: 1个经营案例, {len(interactions)}条交互, {len(journeys)}条旅程, {len(all_claims)}条Claim")


if __name__ == "__main__":
    main()
