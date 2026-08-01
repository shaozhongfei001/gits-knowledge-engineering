#!/usr/bin/env python3
"""Semantic/rule contract runtime gate (CI, fail-closed).

Validates that generated semantic and rule contract artifacts are well-formed
and self-consistent. Exit 0 only if ALL checks pass; any failure exits 2.

Read-only: inspects generated/ and specs/ sources; writes nothing.
"""
from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SHACL_TTL = ROOT / "generated" / "semantic" / "gits-core.shacl.ttl"
SCHEMA_JSON = ROOT / "generated" / "semantic" / "gits-core.schema.json"
DMN_XML = ROOT / "generated" / "rules" / "claim-reconciliation.normalized.dmn"
LINKML_YAML = ROOT / "specs" / "semantic" / "gits-core.linkml.yaml"

SH_NS = "http://www.w3.org/ns/shacl#"
DMN_NS = "https://www.omg.org/spec/DMN/20230324/MODEL/"

GATE_PREFIX = "semantic-rule-gate"


def _fail(reason: str) -> int:
    print(f"{GATE_PREFIX}: FAIL: {reason}", file=sys.stderr)
    return 2


def check_shacl() -> None:
    import rdflib  # local import keeps the module importable without rdflib for --help

    graph = rdflib.Graph()
    graph.parse(str(SHACL_TTL), format="turtle")
    rdf_type = rdflib.URIRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    node_shapes = list(graph.triples((None, rdf_type, rdflib.URIRef(f"{SH_NS}NodeShape"))))
    assert len(node_shapes) >= 1, "no sh:NodeShape instances found"
    print(f"{GATE_PREFIX}: SHACL: PASS")


def check_schema_json() -> None:
    with SCHEMA_JSON.open("r", encoding="utf-8") as fh:
        schema = json.load(fh)
    assert isinstance(schema, dict) and schema, "schema is empty or not a JSON object"
    print(f"{GATE_PREFIX}: Schema JSON: PASS")


def check_dmn() -> None:
    tree = ET.parse(str(DMN_XML))
    root = tree.getroot()
    tag = root.tag
    # Strip XML namespace from root tag for the 'definitions' suffix check.
    local_tag = tag.rsplit("}", 1)[-1] if "}" in tag else tag
    has_decision = root.find(f"{{{DMN_NS}}}decision") is not None
    assert local_tag.endswith("definitions") or has_decision, (
        f"root tag '{local_tag}' is not 'definitions' and no decision element found"
    )
    print(f"{GATE_PREFIX}: DMN: PASS")


def check_linkml() -> None:
    import yaml  # PyYAML

    with LINKML_YAML.open("r", encoding="utf-8") as fh:
        doc = yaml.safe_load(fh)
    assert isinstance(doc, dict), "LinkML document is not a mapping"
    assert any(key in doc for key in ("classes", "schemas", "name")), (
        "LinkML document missing 'classes'/'schemas'/'name' key"
    )
    print(f"{GATE_PREFIX}: LinkML: PASS")


CHECKS = (
    ("SHACL", check_shacl),
    ("Schema JSON", check_schema_json),
    ("DMN", check_dmn),
    ("LinkML", check_linkml),
)


def main() -> int:
    for label, fn in CHECKS:
        try:
            fn()
        except Exception as exc:  # noqa: BLE001 - fail-closed gate
            return _fail(f"{label}: {exc}")
    print(f"{GATE_PREFIX}: PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
