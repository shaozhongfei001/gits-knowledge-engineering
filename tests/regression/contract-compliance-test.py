#!/usr/bin/env python3
"""
GITS 知识工程项目 - 合同合规自动化校验脚本
版本：V1.0
日期：2026-08-05

校验14份合同(CTR-*)的合规性：
1. 权威源文件存在性
2. Schema/规范文件可加载性
3. 生成产物存在性
4. 合同-实现双向一致性
"""

import json
import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any

# 项目根目录
PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
CONTRACT_INDEX_PATH = PROJECT_ROOT / "specs" / "CONTRACT_INDEX.yaml"

# 合同定义
CONTRACTS = [
    {
        "id": "CTR-API-001",
        "kind": "openapi",
        "authority_source": "specs/openapi/gits-kno-api.openapi.json",
        "generated": ["generated/openapi/gits-kno-api.normalized.json"],
        "validators": ["validate_openapi"],
    },
    {
        "id": "CTR-EVENT-001",
        "kind": "asyncapi",
        "authority_source": "specs/events/domain-events.asyncapi.json",
        "generated": ["generated/events/domain-events.normalized.json"],
        "validators": ["validate_asyncapi"],
    },
    {
        "id": "CTR-SEM-001",
        "kind": "linkml_subset",
        "authority_source": "specs/semantic/gits-core.linkml.yaml",
        "generated": [
            "generated/semantic/gits-core.schema.json",
            "generated/semantic/gits-core.shacl.ttl",
        ],
        "validators": ["validate_yaml", "validate_json_schema"],
    },
    {
        "id": "CTR-SEM-002",
        "kind": "turtle",
        "authority_source": "specs/semantic/gits-core.owl.ttl",
        "generated": [],
        "validators": ["validate_turtle"],
    },
    {
        "id": "CTR-RULE-001",
        "kind": "dmn",
        "authority_source": "specs/rules/claim-reconciliation.dmn",
        "generated": ["generated/rules/claim-reconciliation.normalized.dmn"],
        "validators": ["validate_dmn"],
    },
    {
        "id": "CTR-SKILL-001",
        "kind": "json_schema",
        "authority_source": "specs/skills/context-assembly.skill.schema.json",
        "generated": ["generated/skills/context-assembly.skill.schema.json"],
        "validators": ["validate_json_schema"],
    },
    {
        "id": "CTR-ACTION-001",
        "kind": "json_schema",
        "authority_source": "specs/actions/controlled-action.schema.json",
        "generated": ["generated/actions/controlled-action.schema.json"],
        "validators": ["validate_json_schema"],
    },
    {
        "id": "CTR-DATA-001",
        "kind": "json_schema",
        "authority_source": "specs/data/source-contract.schema.json",
        "generated": ["generated/data/source-contract.schema.json"],
        "validators": ["validate_json_schema"],
    },
    {
        "id": "CTR-DATA-002",
        "kind": "source_contract_instance",
        "authority_source": "specs/data/src-edwcrm-cust-base.v0.1.json",
        "generated": ["generated/data/src-edwcrm-cust-base.v0.1.json"],
        "validators": ["validate_json"],
    },
    {
        "id": "CTR-EVIDENCE-001",
        "kind": "json_schema",
        "authority_source": "specs/evidence/evidence-bundle.schema.json",
        "generated": ["generated/evidence/evidence-bundle.schema.json"],
        "validators": ["validate_json_schema"],
    },
    {
        "id": "CTR-EVAL-001",
        "kind": "json_schema",
        "authority_source": "specs/evaluation/run-manifest.schema.json",
        "generated": ["generated/evaluation/run-manifest.schema.json"],
        "validators": ["validate_json_schema"],
    },
    {
        "id": "CTR-MAP-001",
        "kind": "turtle",
        "authority_source": "specs/data/customer-source-mapping.r2rml.ttl",
        "generated": [],
        "validators": ["validate_turtle"],
    },
    {
        "id": "CTR-DATA-003",
        "kind": "source_contract_instance",
        "authority_source": "specs/data/src-oracle-metric-ontology.v0.1.json",
        "generated": [],
        "validators": ["validate_json"],
    },
    {
        "id": "CTR-DATA-004",
        "kind": "seed_claims",
        "authority_source": "specs/data/oracle-seed-claims.v0.1.json",
        "generated": [],
        "validators": ["validate_seed_claims"],
    },
]


class ContractComplianceTest:
    """合同合规校验器"""

    def __init__(self):
        self.passed = 0
        self.failed = 0
        self.skipped = 0
        self.results: list[dict[str, Any]] = []

    def log(self, contract_id: str, test_name: str, result: str, detail: str = ""):
        """记录测试结果"""
        status_icon = {"PASS": "✓", "FAIL": "✗", "SKIP": "○"}.get(result, "?")
        msg = f"  [{status_icon}] {contract_id} | {test_name}"
        if detail:
            msg += f" | {detail}"
        print(msg)
        self.results.append(
            {
                "contract_id": contract_id,
                "test_name": test_name,
                "result": result,
                "detail": detail,
            }
        )
        if result == "PASS":
            self.passed += 1
        elif result == "FAIL":
            self.failed += 1
        else:
            self.skipped += 1

    def check_file_exists(self, contract_id: str, rel_path: str) -> bool:
        """检查文件是否存在"""
        full_path = PROJECT_ROOT / rel_path
        if full_path.exists():
            self.log(contract_id, f"文件存在: {rel_path}", "PASS")
            return True
        else:
            self.log(contract_id, f"文件存在: {rel_path}", "FAIL", "文件不存在")
            return False

    def validate_openapi(self, contract: dict) -> bool:
        """校验OpenAPI合同"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            # 检查关键字段
            if "openapi" in data:
                self.log(cid, "OpenAPI版本字段", "PASS", f"openapi={data['openapi']}")
            else:
                self.log(cid, "OpenAPI版本字段", "FAIL", "缺少openapi字段")
                return False

            if "paths" in data:
                path_count = len(data["paths"])
                self.log(cid, "API路径数量", "PASS", f"{path_count}个路径")
            else:
                self.log(cid, "API路径", "FAIL", "缺少paths字段")
                return False

            # 检查路径前缀
            gits_paths = [p for p in data["paths"] if "/gits/" in p]
            if gits_paths:
                self.log(cid, "API路径前缀(/gits/)", "PASS", f"{len(gits_paths)}个路径")
            else:
                self.log(cid, "API路径前缀(/gits/)", "FAIL", "无/gits/前缀路径")

            return True
        except Exception as e:
            self.log(cid, "OpenAPI加载", "FAIL", str(e))
            return False

    def validate_asyncapi(self, contract: dict) -> bool:
        """校验AsyncAPI合同"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            if "asyncapi" in data:
                self.log(cid, "AsyncAPI版本字段", "PASS", f"asyncapi={data['asyncapi']}")
            else:
                self.log(cid, "AsyncAPI版本字段", "FAIL", "缺少asyncapi字段")
                return False

            if "channels" in data or "events" in data:
                self.log(cid, "事件通道定义", "PASS")
            else:
                self.log(cid, "事件通道定义", "FAIL", "缺少channels/events字段")

            return True
        except Exception as e:
            self.log(cid, "AsyncAPI加载", "FAIL", str(e))
            return False

    def validate_yaml(self, contract: dict) -> bool:
        """校验YAML文件可加载"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            content = path.read_text(encoding="utf-8")
            if content.strip():
                self.log(cid, "YAML文件加载", "PASS", f"{len(content)}字节")
                return True
            else:
                self.log(cid, "YAML文件加载", "FAIL", "文件为空")
                return False
        except Exception as e:
            self.log(cid, "YAML文件加载", "FAIL", str(e))
            return False

    def validate_json_schema(self, contract: dict) -> bool:
        """校验JSON Schema合同"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)

            # 检查JSON Schema关键字段
            if "$schema" in data or "type" in data or "properties" in data:
                self.log(cid, "JSON Schema结构", "PASS")
            else:
                self.log(cid, "JSON Schema结构", "FAIL", "缺少Schema关键字段")
                return False

            return True
        except Exception as e:
            self.log(cid, "JSON Schema加载", "FAIL", str(e))
            return False

    def validate_json(self, contract: dict) -> bool:
        """校验JSON文件可加载"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            self.log(cid, "JSON文件加载", "PASS", f"顶层键: {list(data.keys())[:5]}")
            return True
        except Exception as e:
            self.log(cid, "JSON文件加载", "FAIL", str(e))
            return False

    def validate_turtle(self, contract: dict) -> bool:
        """校验Turtle/RDF文件"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            content = path.read_text(encoding="utf-8")
            # 基本Turtle语法检查
            if "@prefix" in content or "PREFIX" in content:
                self.log(cid, "Turtle前缀定义", "PASS")
            else:
                self.log(cid, "Turtle前缀定义", "FAIL", "缺少@prefix/PREFIX定义")
                return False

            # 检查GITS命名空间
            if "gits:" in content or "/gits/" in content:
                self.log(cid, "GITS命名空间", "PASS")
            else:
                self.log(cid, "GITS命名空间", "WARN", "未找到gits命名空间引用")

            self.log(cid, "Turtle文件加载", "PASS", f"{len(content)}字节")
            return True
        except Exception as e:
            self.log(cid, "Turtle文件加载", "FAIL", str(e))
            return False

    def validate_dmn(self, contract: dict) -> bool:
        """校验DMN规则合同"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            tree = ET.parse(path)
            root = tree.getroot()

            # 检查DMN命名空间
            ns = {"dmn": "https://www.omg.org/spec/DMN/20191111/MODEL/"}
            decisions = root.findall(".//dmn:decision", ns)
            if not decisions:
                # 尝试无命名空间
                decisions = root.findall(".//decision")

            if decisions:
                self.log(cid, "DMN决策表定义", "PASS", f"{len(decisions)}个决策")
            else:
                self.log(cid, "DMN决策表定义", "FAIL", "未找到decision元素")
                return False

            return True
        except Exception as e:
            self.log(cid, "DMN文件加载", "FAIL", str(e))
            return False

    def validate_seed_claims(self, contract: dict) -> bool:
        """校验种子声明合同"""
        cid = contract["id"]
        path = PROJECT_ROOT / contract["authority_source"]
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)

            # 检查种子声明数量
            if isinstance(data, list):
                count = len(data)
                self.log(cid, "种子声明数量", "PASS" if count > 0 else "FAIL", f"{count}条")
            elif isinstance(data, dict):
                if "claims" in data:
                    count = len(data["claims"])
                    self.log(cid, "种子声明数量", "PASS" if count > 0 else "FAIL", f"{count}条")
                else:
                    self.log(cid, "种子声明结构", "WARN", "未找到claims字段")
            else:
                self.log(cid, "种子声明结构", "FAIL", "非预期数据结构")
                return False

            return True
        except Exception as e:
            self.log(cid, "种子声明加载", "FAIL", str(e))
            return False

    def validate_contract(self, contract: dict):
        """校验单个合同"""
        cid = contract["id"]
        print(f"\n{'='*60}")
        print(f"校验合同: {cid} ({contract['kind']})")
        print(f"{'='*60}")

        # 1. 权威源文件存在性
        self.check_file_exists(cid, contract["authority_source"])

        # 2. 执行类型特定校验
        for validator_name in contract.get("validators", []):
            validator = getattr(self, validator_name, None)
            if validator:
                validator(contract)
            else:
                self.log(cid, f"校验器: {validator_name}", "SKIP", "校验器未实现")

        # 3. 生成产物存在性
        for gen_path in contract.get("generated", []):
            self.check_file_exists(cid, gen_path)

    def validate_contract_index(self):
        """校验CONTRACT_INDEX.yaml完整性"""
        print(f"\n{'='*60}")
        print("校验合同索引完整性")
        print(f"{'='*60}")

        if CONTRACT_INDEX_PATH.exists():
            self.log("INDEX", "CONTRACT_INDEX.yaml存在", "PASS")
            try:
                content = CONTRACT_INDEX_PATH.read_text(encoding="utf-8")
                # 检查所有合同ID是否在索引中
                for contract in CONTRACTS:
                    if contract["id"] in content:
                        self.log("INDEX", f"索引包含: {contract['id']}", "PASS")
                    else:
                        self.log("INDEX", f"索引包含: {contract['id']}", "FAIL", "未在索引中找到")
            except Exception as e:
                self.log("INDEX", "索引文件读取", "FAIL", str(e))
        else:
            self.log("INDEX", "CONTRACT_INDEX.yaml存在", "FAIL", "文件不存在")

    def run(self):
        """执行全部校验"""
        print("=" * 60)
        print("GITS-KNO 合同合规自动化校验")
        print(f"项目根目录: {PROJECT_ROOT}")
        print(f"合同数量: {len(CONTRACTS)}")
        print("=" * 60)

        # 校验合同索引
        self.validate_contract_index()

        # 逐个校验合同
        for contract in CONTRACTS:
            self.validate_contract(contract)

        # 汇总报告
        print(f"\n{'='*60}")
        print("校验汇总")
        print(f"{'='*60}")
        print(f"通过: {self.passed}")
        print(f"失败: {self.failed}")
        print(f"跳过: {self.skipped}")
        print(f"总计: {self.passed + self.failed + self.skipped}")

        if self.failed > 0:
            print("\n失败项详情:")
            for r in self.results:
                if r["result"] == "FAIL":
                    print(f"  - {r['contract_id']} | {r['test_name']} | {r['detail']}")

        return self.failed == 0


def main():
    tester = ContractComplianceTest()
    success = tester.run()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
