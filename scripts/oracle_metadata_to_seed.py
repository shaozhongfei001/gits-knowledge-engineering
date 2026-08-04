#!/usr/bin/env python3
"""
Oracle元数据 -> GITS知识引擎种子数据转换脚本

将Oracle数据集市SQLite编目中的指标口径、字段血缘、衍生字段等
转化为GITS项目的知识声明(Claim)种子数据JSON。

映射关系：
- metric_definition -> SYSTEM_FACT Claim (指标口径事实)
- field_lineage -> SYSTEM_FACT Claim (数据血缘事实)
- object_lineage -> SYSTEM_FACT Claim (对象血缘事实)
- derived_field -> SYSTEM_FACT Claim (衍生字段定义)
- analysis_issue -> RISK_SIGNAL Claim (分析议题风险信号)
"""

import json
import sqlite3
import uuid
from datetime import datetime
from typing import Any


def to_uuid(text: str) -> str:
    """将文本ID转化为确定性UUID5。"""
    return str(uuid.uuid5(uuid.NAMESPACE_DNS, f"gits:oracle:{text}"))


def now_iso() -> str:
    return datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")


def extract_metrics(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取指标定义为CLAIM种子数据。"""
    cursor.execute("""
        SELECT metric_id, metric_name_cn, metric_name_en, business_definition,
               formula_expression, numerator_definition, denominator_definition,
               statistical_grain, unit, metric_status, confidence_level,
               source_tables, source_columns, target_schema, target_table, target_column,
               producer_object_owner, producer_object_name, producer_object_type,
               source_hash, business_confirmation_required
        FROM metric_definition
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    claims = []
    for row in rows:
        d = dict(zip(cols, row))
        claim_id = to_uuid(d.get("metric_id", "unknown"))

        # 构建声明语句
        parts = []
        if d.get("metric_name_cn"):
            parts.append(f"指标[{d['metric_name_cn']}]")
        if d.get("metric_name_en"):
            parts.append(f"({d['metric_name_en']})")
        if d.get("business_definition"):
            parts.append(f"业务定义: {d['business_definition'][:200]}")
        if d.get("formula_expression"):
            parts.append(f"公式: {d['formula_expression'][:200]}")

        statement = " ".join(parts) if parts else f"未知指标 {claim_id}"

        # 根据metric_status映射Claim状态
        status_map = {
            "APPROVED": "VERIFIED_FACT",
            "OWNER_CONFIRMED": "VERIFIED_FACT",
            "CANDIDATE": "PROPOSED",
            "CANDIDATE_NOT_SIGNED": "PROPOSED",
            "CODE_DISCOVERED": "PROPOSED",
        }
        claim_status = status_map.get(d.get("metric_status", ""), "PROPOSED")

        claim = {
            "claimId": claim_id,
            "claimType": "SYSTEM_FACT",
            "status": claim_status,
            "statement": statement,
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTable": "metric_definition",
                "sourceRecordId": d.get("metric_id"),
                "sourceHash": d.get("source_hash"),
                "confidenceLevel": d.get("confidence_level"),
                "metricStatus": d.get("metric_status"),
                "statisticalGrain": d.get("statistical_grain"),
                "unit": d.get("unit"),
                "numeratorDefinition": d.get("numerator_definition"),
                "denominatorDefinition": d.get("denominator_definition"),
                "dataLineage": {
                    "sourceTables": d.get("source_tables"),
                    "sourceColumns": d.get("source_columns"),
                    "targetSchema": d.get("target_schema"),
                    "targetTable": d.get("target_table"),
                    "targetColumn": d.get("target_column"),
                },
                "producer": {
                    "owner": d.get("producer_object_owner"),
                    "objectName": d.get("producer_object_name"),
                    "objectType": d.get("producer_object_type"),
                },
                "businessConfirmationRequired": d.get("business_confirmation_required"),
            },
        }
        claims.append(claim)

    return claims


def extract_field_lineage(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取字段血缘为CLAIM种子数据。"""
    cursor.execute("""
        SELECT lineage_id, source_owner, source_object, source_column,
               transformation_type, transformation_expression, filters, group_by,
               target_owner, target_object, target_column,
               producer_owner, producer_object, source_hash, confidence
        FROM field_lineage
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    claims = []
    for row in rows:
        d = dict(zip(cols, row))
        claim_id = to_uuid(f"lineage:{d.get('lineage_id', 'unknown')}")

        source_ref = f"{d.get('source_owner', '?')}.{d.get('source_object', '?')}.{d.get('source_column', '?')}"
        target_ref = f"{d.get('target_owner', '?')}.{d.get('target_object', '?')}.{d.get('target_column', '?')}"

        statement = (
            f"字段血缘: {source_ref} -> {target_ref} "
            f"[{d.get('transformation_type', 'UNKNOWN')}]"
        )
        if d.get("transformation_expression"):
            statement += f" | {d['transformation_expression'][:100]}"

        claim = {
            "claimId": claim_id,
            "claimType": "SYSTEM_FACT",
            "status": "VERIFIED_FACT" if d.get("confidence") == "CODE_CONFIRMED" else "PROPOSED",
            "statement": statement,
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTable": "field_lineage",
                "sourceRecordId": d.get("lineage_id"),
                "sourceHash": d.get("source_hash"),
                "confidence": d.get("confidence"),
                "lineage": {
                    "source": {
                        "owner": d.get("source_owner"),
                        "object": d.get("source_object"),
                        "column": d.get("source_column"),
                    },
                    "target": {
                        "owner": d.get("target_owner"),
                        "object": d.get("target_object"),
                        "column": d.get("target_column"),
                    },
                    "producer": {
                        "owner": d.get("producer_owner"),
                        "object": d.get("producer_object"),
                    },
                    "transformation": {
                        "type": d.get("transformation_type"),
                        "expression": d.get("transformation_expression"),
                        "filters": d.get("filters"),
                        "groupBy": d.get("group_by"),
                    },
                },
            },
        }
        claims.append(claim)

    return claims


def extract_object_lineage(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取对象血缘为CLAIM种子数据。"""
    cursor.execute("""
        SELECT lineage_id, source_owner, source_object, producer_owner,
               producer_object, producer_type, target_owner, target_object, confidence
        FROM object_lineage
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    claims = []
    for row in rows:
        d = dict(zip(cols, row))
        claim_id = to_uuid(f"obj_lineage:{d.get('lineage_id', 'unknown')}")

        source_ref = f"{d.get('source_owner', '?')}.{d.get('source_object', '?')}"
        target_ref = f"{d.get('target_owner', '?')}.{d.get('target_object', '?')}"

        statement = f"对象血缘: {source_ref} -> {target_ref} [{d.get('producer_type', 'UNKNOWN')}]"

        claim = {
            "claimId": claim_id,
            "claimType": "SYSTEM_FACT",
            "status": "VERIFIED_FACT" if d.get("confidence") == "CODE_CONFIRMED" else "PROPOSED",
            "statement": statement,
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTable": "object_lineage",
                "sourceRecordId": d.get("lineage_id"),
                "confidence": d.get("confidence"),
                "lineage": {
                    "source": {
                        "owner": d.get("source_owner"),
                        "object": d.get("source_object"),
                    },
                    "producer": {
                        "owner": d.get("producer_owner"),
                        "object": d.get("producer_object"),
                        "type": d.get("producer_type"),
                    },
                    "target": {
                        "owner": d.get("target_owner"),
                        "object": d.get("target_object"),
                    },
                },
            },
        }
        claims.append(claim)

    return claims


def extract_derived_fields(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取衍生字段为CLAIM种子数据。"""
    cursor.execute("""
        SELECT derived_field_id, target_owner, target_object, target_column,
               business_name_cn, business_description_cn, field_category,
               source_fields, transformation_expression, transformation_type,
               producer_program, source_hash, confidence_level, ambiguities
        FROM derived_field
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    claims = []
    for row in rows:
        d = dict(zip(cols, row))
        claim_id = to_uuid(f"derived:{d.get('derived_field_id', 'unknown')}")

        target_ref = f"{d.get('target_owner', '?')}.{d.get('target_object', '?')}.{d.get('target_column', '?')}"
        biz_name = d.get("business_name_cn") or d.get("target_column", "?")

        statement = f"衍生字段[{biz_name}] -> {target_ref} [{d.get('transformation_type', 'UNKNOWN')}]"
        if d.get("business_description_cn"):
            statement += f" | {d['business_description_cn'][:100]}"

        claim = {
            "claimId": claim_id,
            "claimType": "SYSTEM_FACT",
            "status": "VERIFIED_FACT" if d.get("confidence_level") == "CODE_CONFIRMED" else "PROPOSED",
            "statement": statement,
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTable": "derived_field",
                "sourceRecordId": d.get("derived_field_id"),
                "sourceHash": d.get("source_hash"),
                "confidenceLevel": d.get("confidence_level"),
                "fieldCategory": d.get("field_category"),
                "target": {
                    "owner": d.get("target_owner"),
                    "object": d.get("target_object"),
                    "column": d.get("target_column"),
                },
                "businessName": d.get("business_name_cn"),
                "businessDescription": d.get("business_description_cn"),
                "sourceFields": d.get("source_fields"),
                "transformation": {
                    "type": d.get("transformation_type"),
                    "expression": d.get("transformation_expression"),
                },
                "producerProgram": d.get("producer_program"),
                "ambiguities": d.get("ambiguities"),
            },
        }
        claims.append(claim)

    return claims


def extract_analysis_issues(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取分析议题为RISK_SIGNAL Claim。"""
    cursor.execute("""
        SELECT issue_id, severity, category, object_ref, message, status
        FROM analysis_issue
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    claims = []
    for row in rows:
        d = dict(zip(cols, row))
        claim_id = to_uuid(f"issue:{d.get('issue_id', 'unknown')}")

        statement = f"[{d.get('severity', 'UNKNOWN')}] {d.get('category', '?')}: {d.get('message', '')}"

        claim = {
            "claimId": claim_id,
            "claimType": "RISK_SIGNAL",
            "status": "PROPOSED",
            "statement": statement,
            "recordedAt": now_iso(),
            "evidence": {
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTable": "analysis_issue",
                "sourceRecordId": d.get("issue_id"),
                "severity": d.get("severity"),
                "category": d.get("category"),
                "objectRef": d.get("object_ref"),
                "issueStatus": d.get("status"),
            },
        }
        claims.append(claim)

    return claims


def main():
    db_path = "/home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite"
    output_dir = "/home/szf/dev/gits-knowledge-engineering/specs/data"

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    print("提取指标定义...")
    metrics = extract_metrics(cursor)
    print(f"  提取 {len(metrics)} 条指标")

    print("提取字段血缘...")
    field_lineage = extract_field_lineage(cursor)
    print(f"  提取 {len(field_lineage)} 条字段血缘")

    print("提取对象血缘...")
    object_lineage = extract_object_lineage(cursor)
    print(f"  提取 {len(object_lineage)} 条对象血缘")

    print("提取衍生字段...")
    derived_fields = extract_derived_fields(cursor)
    print(f"  提取 {len(derived_fields)} 条衍生字段")

    print("提取分析议题...")
    issues = extract_analysis_issues(cursor)
    print(f"  提取 {len(issues)} 条分析议题")

    conn.close()

    # 组装完整种子数据
    seed_data = {
        "version": "0.1.0",
        "generatedAt": now_iso(),
        "source": "ORACLE_EDWCRM_METADATA",
        "sourceDatabase": db_path,
        "summary": {
            "metricClaims": len(metrics),
            "fieldLineageClaims": len(field_lineage),
            "objectLineageClaims": len(object_lineage),
            "derivedFieldClaims": len(derived_fields),
            "riskSignalClaims": len(issues),
            "totalClaims": len(metrics) + len(field_lineage) + len(object_lineage) + len(derived_fields) + len(issues),
        },
        "claims": {
            "metrics": metrics,
            "fieldLineage": field_lineage,
            "objectLineage": object_lineage,
            "derivedFields": derived_fields,
            "riskSignals": issues,
        },
    }

    output_path = f"{output_dir}/oracle-seed-claims.v0.1.json"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(seed_data, f, ensure_ascii=False, indent=2)

    print(f"\n种子数据已写入: {output_path}")
    print(f"总计: {seed_data['summary']['totalClaims']} 条Claim")


if __name__ == "__main__":
    main()
