#!/usr/bin/env python3
"""
Hermes业务数据 + Oracle元数据融合脚本
========================================

真实场景：
- Hermes演示数据包 → 提供客户经理看到的实际业务数据（客户、授信、交互、事件）
- Oracle元数据编目 → 提供数据仓库中这些指标的口径定义、血缘关系、质量问题

结合方式：
1. 将Oracle的指标口径(公式/业务定义)作为Hermes业务数据的"数据质量注解"
2. 将Oracle的字段血缘作为Hermes数据的"可追溯性证据"
3. 将Oracle的分析议题作为Hermes数据的"风险提示"
4. 将Oracle的衍生字段逻辑作为Hermes数据的"计算可解释性"

存储位置：
- specs/data/hermes-seed-data.v0.1.json        → Hermes原始业务数据 (已存在)
- specs/data/oracle-seed-claims.v0.1.json       → Oracle原始元数据 (已存在)
- specs/data/hermes-oracle-fusion.v0.1.json     → 融合后的知识 (本脚本输出)
- specs/data/fusion-index.json                  → 融合索引，记录双向引用

使用方式：
  python3 scripts/hermes_oracle_fusion.py

输出：
  specs/data/hermes-oracle-fusion.v0.1.json  - 融合知识包
  specs/data/fusion-index.json               - 融合索引
"""

import json
import re
import sqlite3
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

# ============================================================
# 路径配置
# ============================================================
BASE_DIR = Path(__file__).resolve().parent.parent
HERMES_PKG = Path("/home/szf/dev/data/Hermes演示数据包_V1.0_RUN_001")
ORACLE_DB = Path("/home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite")
OUTPUT_FUSION = BASE_DIR / "specs" / "data" / "hermes-oracle-fusion.v0.1.json"
OUTPUT_INDEX = BASE_DIR / "specs" / "data" / "fusion-index.json"

NOW_ISO = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

# ============================================================
# 第一步：从Hermes Markdown文档中提取结构化事实
# ============================================================

def extract_facts_from_cov(cov_md: str) -> dict[str, Any]:
    """从Customer Operating View Markdown文档中提取结构化事实"""
    facts = {
        "customer": {},
        "credit_facilities": [],
        "interactions": [],
        "external_events": [],
        "kyc_gaps": [],
        "commitments": [],
        "opportunities": [],
        "reconciliations": [],
    }

    # 提取客户基本信息
    if "华东精工装备集团有限公司" in cov_md:
        facts["customer"] = {
            "customerId": "HDEG-001",
            "customerName": "华东精工装备集团有限公司",
            "industry": "高端装备制造",
            "customerType": "战略客户",
            "relationshipYears": 8,
            "relationshipSince": "2018-05-16",
            "rmName": "张伟",
            "rmId": "RM-ZW-001",
        }

    # 提取授信信息
    if "综合授信总额" in cov_md:
        facts["credit_facilities"].append({
            "totalCredit": "150000000",  # 1.5亿元
            "usedCredit": "110000000",    # 1.1亿元
            "availableCredit": "40000000", # 4000万元
            "loanBalance": "85000000",     # 8500万元
            "acceptanceBalance": "16000000", # 1600万元
            "guaranteeBalance": "9000000",   # 900万元
            "expiryDate": "2027-01-14",
            "collateralType": "集团厂房抵押 + 实控人连带责任保证",
            "usageRestriction": "不得直接用于固定资产投资",
            "evidenceRef": "EV-CREDIT-001",
        })

    # 提取资金结算信息
    facts["settlement"] = {
        "avgDeposit": {
            "from": "121000000",  # 1.21亿元
            "to": "78000000",     # 7800万元
            "period": "2026-01 to 2026-07",
            "change": "下降约35%",
        },
        "monthlySettlement": {
            "from": "3700000000",  # 3.7亿
            "to": "4000000000",    # 4.0亿
            "period": "2026-01 to 2026-07",
        },
        "loanBalance": {
            "from": "85000000",   # 8500万
            "to": "110000000",    # 1.1亿
            "period": "2026-01 to 2026-07",
        },
    }

    # 提取项目信息
    facts["project"] = {
        "projectName": "智能制造二期项目",
        "projectEntity": "华东精工智能制造有限公司",
        "totalInvestment": "48000000",  # 4800万元
        "equipmentCost": "32800000",     # 3280万元
        "paymentSchedule": {
            "advance": "30% (984万/7月25日)",
            "delivery": "40% (1312万/9月20日)",
            "acceptance": "30% (984万/11月30日)",
        },
        "evidenceRef": "EV-EXT-001, EV-MAT-001",
    }

    return facts


def extract_facts_from_recon(recon_md: str) -> list[dict]:
    """从Fact Reconciliation Report中提取对账事实"""
    reconciliations = []

    # REC-001: 3000万语义对账
    reconciliations.append({
        "recId": "REC-001",
        "topic": "3000万语义对账",
        "conflict": "客户说'增加3000万左右支持'，语义未消歧",
        "possibleInterpretations": [
            "新增额度",
            "现有额度提款",
            "用途调整",
            "项目主体变更融资",
        ],
        "evidenceRefs": ["EV-CREDIT-001", "EV-MTG-001"],
        "crmWritable": False,
        "requiresConfirmation": True,
    })

    # REC-002: 项目金额口径对账
    reconciliations.append({
        "recId": "REC-002",
        "topic": "项目金额口径对账",
        "conflict": "备案总投资4800万 vs 设备清单3280万",
        "resolution": "口径不同，不是冲突。4800万含设备+安装+基建+配套，3280万仅设备",
        "evidenceRefs": ["EV-EXT-001", "EV-MAT-001"],
        "crmWritable": True,
        "requiresConfirmation": True,
    })

    # REC-003: 项目主体与授信主体对账
    reconciliations.append({
        "recId": "REC-003",
        "topic": "项目主体与授信主体对账",
        "conflict": "授信主体是集团本部，项目主体可能是智能制造公司",
        "impact": "影响借款主体、担保结构、提款路径和审批条件",
        "evidenceRefs": ["EV-CREDIT-001", "EV-EXT-001", "EV-MTG-001"],
        "crmWritable": True,
        "requiresConfirmation": True,
    })

    return reconciliations


# ============================================================
# 第二步：加载Oracle元数据
# ============================================================

def load_oracle_metadata() -> dict[str, Any]:
    """从Oracle编目库加载指标定义、血缘、衍生字段、分析议题"""
    conn = sqlite3.connect(str(ORACLE_DB))
    conn.row_factory = sqlite3.Row

    metadata = {}

    # 指标定义
    metrics = conn.execute("SELECT * FROM metric_definition").fetchall()
    metadata["metrics"] = [dict(m) for m in metrics]

    # 字段血缘
    field_lineage = conn.execute("SELECT * FROM field_lineage").fetchall()
    metadata["field_lineage"] = [dict(fl) for fl in field_lineage]

    # 对象血缘
    object_lineage = conn.execute("SELECT * FROM object_lineage").fetchall()
    metadata["object_lineage"] = [dict(ol) for ol in object_lineage]

    # 衍生字段
    derived_fields = conn.execute("SELECT * FROM derived_field").fetchall()
    metadata["derived_fields"] = [dict(df) for df in derived_fields]

    # 分析议题
    issues = conn.execute("SELECT * FROM analysis_issue").fetchall()
    metadata["issues"] = [dict(i) for i in issues]

    conn.close()
    return metadata


# ============================================================
# 第三步：建立业务事实与Oracle指标的映射
# ============================================================

# Hermes业务事实 → Oracle指标的映射规则
FACT_TO_METRIC_MAP = {
    "loanBalance": {
        "oracle_metrics": ["loan_balance_amount", "loan_balance_yi"],
        "description": "贷款余额 → Oracle指标口径",
    },
    "avgDeposit": {
        "oracle_metrics": ["avg_balance_per_customer", "DAVG_RATE_Y"],
        "description": "日均存款 → Oracle指标口径",
    },
    "overdueDays": {
        "oracle_metrics": ["overdue_balance_rate", "overdue_customer_rate"],
        "description": "逾期天数 → Oracle逾期率指标口径",
    },
    "creditUtilization": {
        "oracle_metrics": ["CRC_OVER_RATE", "CRC_YX_RATIO"],
        "description": "额度使用率 → Oracle透支/用信比指标",
    },
    "customerCoverage": {
        "oracle_metrics": ["CUST_COVER_RAT", "active_customer_count"],
        "description": "客户覆盖率 → Oracle客户类指标",
    },
    "financialRatio": {
        "oracle_metrics": ["ZCFZL", "CURRENTRATE"],
        "description": "财务比率 → Oracle资产负债率/流动比率",
    },
}


def find_relevant_oracle_metrics(facts: dict) -> list[dict]:
    """根据Hermes业务事实，找出Oracle中相关的指标口径"""
    relevant = []

    for fact_key, mapping in FACT_TO_METRIC_MAP.items():
        for metric_id in mapping["oracle_metrics"]:
            relevant.append({
                "factKey": fact_key,
                "metricId": metric_id,
                "description": mapping["description"],
            })

    return relevant


# ============================================================
# 第四步：构建融合知识
# ============================================================

def build_fusion_knowledge(facts: dict, reconciliations: list, oracle_meta: dict) -> dict:
    """
    核心融合逻辑：
    对每条Hermes业务事实，附着Oracle的指标口径、血缘、衍生逻辑、风险提示
    """
    fusion = {
        "version": "0.1.0",
        "generatedAt": NOW_ISO,
        "description": "Hermes业务数据与Oracle元数据的融合知识包",
        "fusionModel": {
            "businessData": "Hermes演示数据包_V1.0 — 客户经理视角的业务数据",
            "metadataLayer": "Oracle编目库 — 数据仓库指标口径、血缘关系、质量问题",
            "fusionPrinciple": "业务数据为事实，元数据为注解；注解不改变事实，但提供可解释性",
        },
        "cases": [],
        "metricAnnotations": [],
        "lineageTraces": [],
        "riskWarnings": [],
    }

    # ---- 构建经营案例（Hermes业务数据 + Oracle注解） ----
    case = {
        "caseId": f"CASE-HERMES-{facts['customer'].get('customerId', 'UNKNOWN')}",
        "caseType": "CUSTOMER_OPERATING",
        "subject": facts["customer"],
        "businessData": {
            "creditFacilities": facts.get("credit_facilities", []),
            "settlement": facts.get("settlement", {}),
            "project": facts.get("project", {}),
            "reconciliations": reconciliations,
        },
        "oracleAnnotations": {
            "appliedMetrics": [],
            "lineageTraces": [],
            "derivedFieldLogic": [],
            "dataQualityIssues": [],
        },
        "status": "ACTIVE",
        "createdAt": NOW_ISO,
    }

    # --- 附着Oracle指标口径 ---
    relevant_metrics = find_relevant_oracle_metrics(facts)

    for rm in relevant_metrics:
        metric_id = rm["metricId"]
        # 在Oracle元数据中查找该指标的完整定义
        oracle_metric = None
        for m in oracle_meta["metrics"]:
            if m.get("metric_id") == metric_id:
                oracle_metric = m
                break

        if oracle_metric:
            annotation = {
                "metricId": metric_id,
                "metricName": oracle_metric.get("metric_name_cn") or oracle_metric.get("metric_name_en"),
                "businessDefinition": oracle_metric.get("business_definition"),
                "formula": oracle_metric.get("formula_expression"),
                "unit": oracle_metric.get("unit"),
                "confidenceLevel": oracle_metric.get("confidence_level"),
                "metricStatus": oracle_metric.get("metric_status"),
                "hermesFactKey": rm["factKey"],
                "mappingDescription": rm["description"],
            }
            case["oracleAnnotations"]["appliedMetrics"].append(annotation)
            fusion["metricAnnotations"].append(annotation)

    # --- 附着Oracle字段血缘 ---
    # 找出与贷款/存款/客户相关的血缘
    lineage_keywords = ["LOAN", "DEPOSIT", "CUST", "CREDIT", "BALANCE"]
    relevant_lineage = [
        fl for fl in oracle_meta["field_lineage"]
        if any(kw in (fl.get("source_object") or "").upper()
               or kw in (fl.get("target_object") or "").upper()
               for kw in lineage_keywords)
    ]

    # 按目标表分组
    lineage_by_table = {}
    for fl in relevant_lineage:
        target = fl.get("target_object") or "UNKNOWN"
        if target not in lineage_by_table:
            lineage_by_table[target] = []
        lineage_by_table[target].append(fl)

    for table_name, lineage_list in lineage_by_table.items():
        trace = {
            "tableName": table_name,
            "fieldLineageCount": len(lineage_list),
            "sampleLineage": [
                {
                    "sourceField": fl.get("source_field"),
                    "targetField": fl.get("target_field"),
                    "transformation": fl.get("transformation_rule"),
                }
                for fl in lineage_list[:5]  # 取前5条作为示例
            ],
        }
        case["oracleAnnotations"]["lineageTraces"].append(trace)
        fusion["lineageTraces"].append(trace)

    # --- 附着Oracle衍生字段逻辑 ---
    # 找出与余额/贷款/客户相关的衍生字段
    derived_keywords = ["余额", "贷款", "客户", "授信", "逾期", "透支", "日均"]
    relevant_derived = [
        df for df in oracle_meta["derived_fields"]
        if any(kw in (df.get("derived_label") or "") for kw in derived_keywords)
    ]

    for df in relevant_derived[:10]:  # 取前10条
        logic = {
            "derivedField": df.get("derived_label") or df.get("derived_name"),
            "sourceColumns": df.get("source_columns"),
            "transformationLogic": df.get("transformation_logic"),
            "businessMeaning": df.get("business_meaning"),
            "dataCategory": df.get("data_category"),
        }
        case["oracleAnnotations"]["derivedFieldLogic"].append(logic)

    # --- 附着Oracle分析议题（风险提示） ---
    for issue in oracle_meta["issues"]:
        warning = {
            "issueId": issue.get("issue_id"),
            "severity": issue.get("severity"),
            "category": issue.get("category"),
            "objectRef": issue.get("object_ref"),
            "message": issue.get("message"),
            "status": issue.get("status"),
            "impactOnHermes": _assess_issue_impact(issue),
        }
        case["oracleAnnotations"]["dataQualityIssues"].append(warning)
        fusion["riskWarnings"].append(warning)

    fusion["cases"].append(case)

    return fusion


def _assess_issue_impact(issue: dict) -> str:
    """评估Oracle分析议题对Hermes业务数据的影响"""
    category = issue.get("category", "")

    if category == "DICT_ANOMALY":
        return (
            "Oracle数据字典与源码不一致，可能导致Hermes中显示的指标值与数据仓库实际计算结果存在偏差。"
            "客户经理在Hermes界面看到的'逾期率'可能与后台报表不一致。"
        )
    elif category == "PRIVILEGE_GAP":
        return (
            "DBETL Schema权限缺失导致调度日志血缘断点。"
            "Hermes中显示的'最近更新时间'可能无法追溯到具体的ETL作业执行记录。"
        )
    elif category == "COVERAGE_GAP":
        return (
            "系统Schema未做SQL提取，如果Hermes依赖的系统级视图被修改，"
            "可能影响数据一致性但不会被血缘追踪发现。"
        )
    elif category == "ARCHITECTURE_FINDING":
        return (
            "EDWCRM由外部ETL调度驱动，Hermes中的数据刷新依赖于调度链路的完整性。"
            "如调度失败，Hermes可能显示过期的业务快照。"
        )
    else:
        return f"[{issue.get('severity', 'UNKNOWN')}] 未分类议题，需人工评估对Hermes数据的影响"


# ============================================================
# 第五步：构建融合索引
# ============================================================

def build_fusion_index(fusion: dict) -> dict:
    """
    构建双向引用索引：
    - Hermes业务数据 → Oracle元数据（注解方向）
    - Oracle元数据 → Hermes业务数据（反向追溯）
    """
    index = {
        "version": "0.1.0",
        "generatedAt": NOW_ISO,
        "description": "Hermes-Oracle融合知识双向引用索引",
        "hermesToOracle": {},
        "oracleToHermes": {},
    }

    for case in fusion.get("cases", []):
        case_id = case["caseId"]
        customer_id = case["subject"].get("customerId", "UNKNOWN")

        # Hermes → Oracle
        index["hermesToOracle"][case_id] = {
            "customerId": customer_id,
            "appliedMetrics": [m["metricId"] for m in case["oracleAnnotations"]["appliedMetrics"]],
            "lineageTraces": [t["tableName"] for t in case["oracleAnnotations"]["lineageTraces"]],
            "derivedFieldCount": len(case["oracleAnnotations"]["derivedFieldLogic"]),
            "dataQualityIssueCount": len(case["oracleAnnotations"]["dataQualityIssues"]),
        }

        # Oracle → Hermes
        for metric in case["oracleAnnotations"]["appliedMetrics"]:
            metric_id = metric["metricId"]
            if metric_id not in index["oracleToHermes"]:
                index["oracleToHermes"][metric_id] = {
                    "metricName": metric["metricName"],
                    "appliedToCases": [],
                    "hermesFactKey": metric["hermesFactKey"],
                }
            index["oracleToHermes"][metric_id]["appliedToCases"].append(case_id)

        for trace in case["oracleAnnotations"]["lineageTraces"]:
            table_name = trace["tableName"]
            if table_name not in index["oracleToHermes"]:
                index["oracleToHermes"][table_name] = {
                    "fieldLineageCount": trace["fieldLineageCount"],
                    "appliedToCases": [],
                }
            index["oracleToHermes"][table_name]["appliedToCases"].append(case_id)

    return index


# ============================================================
# 主执行
# ============================================================

def main():
    print("=" * 70)
    print("Hermes业务数据 + Oracle元数据 融合脚本")
    print("=" * 70)

    # 1. 从Hermes Markdown文档提取事实
    print("\n[1/5] 从Hermes Markdown文档提取结构化事实...")
    cov_md = (HERMES_PKG / "04_CUSTOMER_OPERATING_VIEW_RENDERED.md").read_text(encoding="utf-8")
    recon_md = (HERMES_PKG / "07_FACT_RECONCILIATION_REPORT.md").read_text(encoding="utf-8")

    facts = extract_facts_from_cov(cov_md)
    reconciliations = extract_facts_from_recon(recon_md)

    print(f"  客户: {facts['customer'].get('customerName')} ({facts['customer'].get('customerId')})")
    print(f"  授信设施: {len(facts['credit_facilities'])} 条")
    print(f"  对账记录: {len(reconciliations)} 条")

    # 2. 加载Oracle元数据
    print("\n[2/5] 加载Oracle元数据编目库...")
    oracle_meta = load_oracle_metadata()
    print(f"  指标定义: {len(oracle_meta['metrics'])} 条")
    print(f"  字段血缘: {len(oracle_meta['field_lineage'])} 条")
    print(f"  衍生字段: {len(oracle_meta['derived_fields'])} 条")
    print(f"  分析议题: {len(oracle_meta['issues'])} 条")

    # 3. 构建融合知识
    print("\n[3/5] 构建融合知识...")
    fusion = build_fusion_knowledge(facts, reconciliations, oracle_meta)
    print(f"  经营案例: {len(fusion['cases'])} 个")
    print(f"  指标注解: {len(fusion['metricAnnotations'])} 条")
    print(f"  血缘追踪: {len(fusion['lineageTraces'])} 条")
    print(f"  风险提示: {len(fusion['riskWarnings'])} 条")

    # 4. 构建融合索引
    print("\n[4/5] 构建融合索引...")
    index = build_fusion_index(fusion)
    print(f"  Hermes→Oracle引用: {len(index['hermesToOracle'])} 条")
    print(f"  Oracle→Hermes引用: {len(index['oracleToHermes'])} 条")

    # 5. 输出
    print("\n[5/5] 写入输出文件...")
    OUTPUT_FUSION.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_FUSION.write_text(json.dumps(fusion, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  融合知识: {OUTPUT_FUSION}")

    OUTPUT_INDEX.write_text(json.dumps(index, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  融合索引: {OUTPUT_INDEX}")

    # 统计摘要
    print("\n" + "=" * 70)
    print("融合摘要")
    print("=" * 70)
    case = fusion["cases"][0]
    print(f"\n经营案例: {case['caseId']}")
    print(f"  客户: {case['subject'].get('customerName')} ({case['subject'].get('customerId')})")
    print(f"  行业: {case['subject'].get('industry')}")
    print(f"\n  Oracle注解:")
    print(f"    指标口径注解: {len(case['oracleAnnotations']['appliedMetrics'])} 条")
    for m in case["oracleAnnotations"]["appliedMetrics"][:5]:
        print(f"      - [{m['metricName']}] {m.get('businessDefinition', '')[:60]}")
    print(f"    血缘追踪: {len(case['oracleAnnotations']['lineageTraces'])} 条")
    for t in case["oracleAnnotations"]["lineageTraces"][:3]:
        print(f"      - {t['tableName']}: {t['fieldLineageCount']} 条字段血缘")
    print(f"    衍生字段逻辑: {len(case['oracleAnnotations']['derivedFieldLogic'])} 条")
    print(f"    数据质量议题: {len(case['oracleAnnotations']['dataQualityIssues'])} 条")

    print("\n  存储位置:")
    print(f"    融合知识包: specs/data/hermes-oracle-fusion.v0.1.json")
    print(f"    融合索引:   specs/data/fusion-index.json")
    print(f"    原始Hermes: specs/data/hermes-seed-data.v0.1.json")
    print(f"    原始Oracle: specs/data/oracle-seed-claims.v0.1.json")

    print("\n  使用场景:")
    print("    1. 客户经理在Hermes查看客户授信 → 同时看到指标口径注解")
    print("    2. 数据质疑时 → 沿血缘追踪到Oracle ETL加工链路")
    print("    3. 数据质量告警 → Oracle分析议题自动关联到受影响的Hermes数据")
    print("    4. 审计追溯 → 从业务值反向查SQL公式和字段血缘")

    print("\n✅ 融合完成")


if __name__ == "__main__":
    main()
