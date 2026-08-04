#!/usr/bin/env python3
"""
Oracle客户数据 -> GITS经营案例转换脚本

将Oracle数据集市中的客户指标数据(F_CUST_*)提取并转化为
GITS知识工程的OperatingCase种子数据。

映射关系：
- F_CUST_LOAN_IFO -> OperatingCase.evidence.loanMetrics
- F_CUST_DEPR_IFO -> OperatingCase.evidence.depositMetrics
- F_CUST_OWNED_PRODUCT -> OperatingCase.evidence.productHolding
- F_CUST_ZCAMT_IFO -> OperatingCase.evidence.assetMetrics
- F_CUST_SIGN_IFO -> OperatingCase.evidence.channelSigning
- F_CUST_CRC_IFO -> OperatingCase.evidence.creditCardMetrics
- A_ZHCX_CUST_BASE -> OperatingCase.subject
- A_ZHCX_CUST_EXP -> OperatingCase.evidence.customerExtension
"""

import json
import sqlite3
import uuid
from datetime import datetime
from typing import Any


def to_uuid(text: str) -> str:
    """将文本ID转化为确定性UUID5。"""
    return str(uuid.uuid5(uuid.NAMESPACE_DNS, f"gits:oracle:cust:{text}"))


def now_iso() -> str:
    return datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")


def extract_customer_base(cursor: sqlite3.Cursor) -> list[dict[str, Any]]:
    """提取客户基础信息(A_ZHCX_CUST_BASE)。"""
    cursor.execute("""
        SELECT CUSTID, CUSTNAME, CUSTTYPE, CUSTINDUSTRY, CUSTLEVEL,
               CUSTSTATUS, OPEN_DATE, OPEN_TELLER, LAST_UPDATE_DATE,
               DATA_DT
        FROM A_ZHCX_CUST_BASE
        WHERE ROWNUM <= 50  -- 限制样本量
    """)
    rows = cursor.fetchall()
    cols = [d[0] for d in cursor.description]

    customers = []
    for row in rows:
        d = dict(zip(cols, row))
        customer = {
            "customerId": d.get("CUSTID"),
            "customerName": d.get("CUSTNAME"),
            "customerType": d.get("CUSTTYPE"),
            "industry": d.get("CUSTINDUSTRY"),
            "level": d.get("CUSTLEVEL"),
            "status": d.get("CUSTSTATUS"),
            "openDate": str(d.get("OPEN_DATE")) if d.get("OPEN_DATE") else None,
            "openTeller": d.get("OPEN_TELLER"),
            "lastUpdateDate": str(d.get("LAST_UPDATE_DATE")) if d.get("LAST_UPDATE_DATE") else None,
            "dataDate": str(d.get("DATA_DT")) if d.get("DATA_DT") else None,
        }
        customers.append(customer)

    return customers


def extract_loan_metrics(cursor: sqlite3.Cursor, cust_id: str) -> dict[str, Any]:
    """提取贷款指标(F_CUST_LOAN_IFO)。"""
    cursor.execute("""
        SELECT CUSTID, FSLOAN_TONOW, OWER_LOAN_NUM, LOAN_BAL, LOAN_AVG_M,
               LOAN_AVG_Q, LOAN_AVG_Y, GLOAN_BAL, GLOAN_AVG_M, GLOAN_AVG_Q,
               GLOAN_AVG_Y, SLOAN_BAL, SPOAN_AVG_M, SPLOAN_BAL, BLOAN_BAL,
               LOAN_OVERDUE_BAL, LOAN_OVERDUE_RATE, LOAN_NPL_BAL, LOAN_NPL_RATE,
               LOAN_GUARANTEE_BAL, LOAN_MORTGAGE_BAL, LOAN_PLEDGE_BAL,
               LOAN_CREDIT_BAL, LOAN_PRODUCT_COUNT, LOAN_RENEWAL_COUNT,
               LOAN_REPAYMENT_METHOD, LOAN_TERM_AVG, LOAN_RATE_AVG,
               LOAN_PURPOSE_MAIN, LOAN_INDUSTRY_MAIN, DATA_DT
        FROM F_CUST_LOAN_IFO
        WHERE CUSTID = ?
    """, (cust_id,))
    row = cursor.fetchone()
    if not row:
        return {}

    cols = [d[0] for d in cursor.description]
    d = dict(zip(cols, row))

    return {
        "customerId": d.get("CUSTID"),
        "earliestLoanDays": d.get("FSLOAN_TONOW"),
        "loanProductCount": d.get("OWER_LOAN_NUM"),
        "loanBalance": d.get("LOAN_BAL"),
        "loanAvgMonthly": d.get("LOAN_AVG_M"),
        "loanAvgQuarterly": d.get("LOAN_AVG_Q"),
        "loanAvgYearly": d.get("LOAN_AVG_Y"),
        "generalLoanBalance": d.get("GLOAN_BAL"),
        "smallLoanBalance": d.get("SLOAN_BAL"),
        "consumerLoanBalance": d.get("SPOAN_AVG_M"),
        "selfServiceLoanBalance": d.get("SPLOAN_BAL"),
        "nonPerformingLoanBalance": d.get("BLOAN_BAL"),
        "overdueBalance": d.get("LOAN_OVERDUE_BAL"),
        "overdueRate": d.get("LOAN_OVERDUE_RATE"),
        "nplBalance": d.get("LOAN_NPL_BAL"),
        "nplRate": d.get("LOAN_NPL_RATE"),
        "guaranteeBalance": d.get("LOAN_GUARANTEE_BAL"),
        "mortgageBalance": d.get("LOAN_MORTGAGE_BAL"),
        "pledgeBalance": d.get("LOAN_PLEDGE_BAL"),
        "creditBalance": d.get("LOAN_CREDIT_BAL"),
        "renewalCount": d.get("LOAN_RENEWAL_COUNT"),
        "avgTerm": d.get("LOAN_TERM_AVG"),
        "avgRate": d.get("LOAN_RATE_AVG"),
        "mainPurpose": d.get("LOAN_PURPOSE_MAIN"),
        "mainIndustry": d.get("LOAN_INDUSTRY_MAIN"),
        "dataDate": str(d.get("DATA_DT")) if d.get("DATA_DT") else None,
    }


def extract_deposit_metrics(cursor: sqlite3.Cursor, cust_id: str) -> dict[str, Any]:
    """提取存款指标(F_CUST_DEPR_IFO)。"""
    cursor.execute("""
        SELECT CUSTID, DEP_BAL, DEP_AVG_M, DEP_AVG_Q, DEP_AVG_Y,
               SAV_BAL, SAV_AVG_M, CORP_BAL, CORP_AVG_M,
               TIME_DEP_BAL, TIME_DEP_AVG_M, DEMAND_DEP_BAL, DEMAND_DEP_AVG_M,
               DATA_DT
        FROM F_CUST_DEPR_IFO
        WHERE CUSTID = ?
    """, (cust_id,))
    row = cursor.fetchone()
    if not row:
        return {}

    cols = [d[0] for d in cursor.description]
    d = dict(zip(cols, row))

    return {
        "customerId": d.get("CUSTID"),
        "depositBalance": d.get("DEP_BAL"),
        "depositAvgMonthly": d.get("DEP_AVG_M"),
        "depositAvgQuarterly": d.get("DEP_AVG_Q"),
        "depositAvgYearly": d.get("DEP_AVG_Y"),
        "savingsBalance": d.get("SAV_BAL"),
        "savingsAvgMonthly": d.get("SAV_AVG_M"),
        "corporateBalance": d.get("CORP_BAL"),
        "corporateAvgMonthly": d.get("CORP_AVG_M"),
        "timeDepositBalance": d.get("TIME_DEP_BAL"),
        "timeDepositAvgMonthly": d.get("TIME_DEP_AVG_M"),
        "demandDepositBalance": d.get("DEMAND_DEP_BAL"),
        "demandDepositAvgMonthly": d.get("DEMAND_DEP_AVG_M"),
        "dataDate": str(d.get("DATA_DT")) if d.get("DATA_DT") else None,
    }


def extract_product_holding(cursor: sqlite3.Cursor, cust_id: str) -> dict[str, Any]:
    """提取持有产品信息(F_CUST_OWNED_PRODUCT)。"""
    cursor.execute("""
        SELECT CUSTID, PRODUCT_COUNT, PRODUCT_LIST, LAST_PRODUCT_DATE,
               DATA_DT
        FROM F_CUST_OWNED_PRODUCT
        WHERE CUSTID = ?
    """, (cust_id,))
    row = cursor.fetchone()
    if not row:
        return {}

    cols = [d[0] for d in cursor.description]
    d = dict(zip(cols, row))

    return {
        "customerId": d.get("CUSTID"),
        "productCount": d.get("PRODUCT_COUNT"),
        "productList": d.get("PRODUCT_LIST"),
        "lastProductDate": str(d.get("LAST_PRODUCT_DATE")) if d.get("LAST_PRODUCT_DATE") else None,
        "dataDate": str(d.get("DATA_DT")) if d.get("DATA_DT") else None,
    }


def extract_asset_metrics(cursor: sqlite3.Cursor, cust_id: str) -> dict[str, Any]:
    """提取资产指标(F_CUST_ZCAMT_IFO)。"""
    cursor.execute("""
        SELECT CUSTID, ZCAMT_BAL, ZCAMT_AVG_M, ZCAMT_AVG_Q, ZCAMT_AVG_Y,
               DATA_DT
        FROM F_CUST_ZCAMT_IFO
        WHERE CUSTID = ?
    """, (cust_id,))
    row = cursor.fetchone()
    if not row:
        return {}

    cols = [d[0] for d in cursor.description]
    d = dict(zip(cols, row))

    return {
        "customerId": d.get("CUSTID"),
        "totalAssetBalance": d.get("ZCAMT_BAL"),
        "totalAssetAvgMonthly": d.get("ZCAMT_AVG_M"),
        "totalAssetAvgQuarterly": d.get("ZCAMT_AVG_Q"),
        "totalAssetAvgYearly": d.get("ZCAMT_AVG_Y"),
        "dataDate": str(d.get("DATA_DT")) if d.get("DATA_DT") else None,
    }


def build_operating_cases(customers: list[dict], conn: sqlite3.Connection) -> list[dict[str, Any]]:
    """为每个客户构建经营案例。"""
    cursor = conn.cursor()
    cases = []

    for cust in customers:
        cust_id = cust.get("customerId")
        if not cust_id:
            continue

        # 提取各维度指标
        loan_metrics = extract_loan_metrics(cursor, cust_id)
        deposit_metrics = extract_deposit_metrics(cursor, cust_id)
        product_holding = extract_product_holding(cursor, cust_id)
        asset_metrics = extract_asset_metrics(cursor, cust_id)

        # 构建OperatingCase
        case_id = to_uuid(cust_id)
        case = {
            "caseId": case_id,
            "caseType": "CUSTOMER_OPERATING",
            "status": "ACTIVE",
            "subject": {
                "customerId": cust.get("customerId"),
                "customerName": cust.get("customerName"),
                "customerType": cust.get("customerType"),
                "industry": cust.get("industry"),
                "level": cust.get("level"),
                "status": cust.get("status"),
            },
            "evidence": {
                "loanMetrics": loan_metrics if loan_metrics else None,
                "depositMetrics": deposit_metrics if deposit_metrics else None,
                "productHolding": product_holding if product_holding else None,
                "assetMetrics": asset_metrics if asset_metrics else None,
                "sourceSystem": "ORACLE_EDWCRM",
                "sourceTables": [
                    "F_CUST_LOAN_IFO",
                    "F_CUST_DEPR_IFO",
                    "F_CUST_OWNED_PRODUCT",
                    "F_CUST_ZCAMT_IFO",
                ],
            },
            "recordedAt": now_iso(),
            "lastUpdated": cust.get("lastUpdateDate") or now_iso(),
        }
        cases.append(case)

    return cases


def main():
    db_path = "/home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite"
    output_dir = "/home/szf/dev/gits-knowledge-engineering/specs/data"

    conn = sqlite3.connect(db_path)

    cursor = conn.cursor()
    print("提取客户基础信息...")
    customers = extract_customer_base(cursor)
    print(f"  提取 {len(customers)} 个客户")

    print("构建经营案例...")
    cases = build_operating_cases(customers, conn)
    print(f"  构建 {len(cases)} 个经营案例")

    conn.close()

    # 组装输出数据
    output_data = {
        "version": "0.1.0",
        "generatedAt": now_iso(),
        "source": "ORACLE_EDWCRM_CUSTOMER_DATA",
        "sourceDatabase": db_path,
        "summary": {
            "totalCustomers": len(customers),
            "totalCases": len(cases),
            "evidenceDimensions": ["loanMetrics", "depositMetrics", "productHolding", "assetMetrics"],
        },
        "operatingCases": cases,
    }

    output_path = f"{output_dir}/oracle-customer-operating-cases.v0.1.json"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"\n经营案例数据已写入: {output_path}")
    print(f"总计: {len(cases)} 个经营案例")


if __name__ == "__main__":
    main()
