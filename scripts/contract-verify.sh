#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────
# G3.2 合同验证脚本 — 检查 CONTRACT_INDEX.yaml 中引用的文件
# ──────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONTRACT_INDEX="$PROJECT_ROOT/specs/CONTRACT_INDEX.yaml"

FAILED=0
CHECKED=0
MISSING=()

info()  { echo "[INFO]  $*"; }
ok()    { echo "[  OK]  $*"; }
fail()  { echo "[FAIL]  $*"; FAILED=1; }

# ── 1. Check CONTRACT_INDEX.yaml exists ──
if [[ ! -f "$CONTRACT_INDEX" ]]; then
    fail "CONTRACT_INDEX.yaml not found at $CONTRACT_INDEX"
    echo "Result: FAIL (1 check failed)"
    exit 1
fi
ok "CONTRACT_INDEX.yaml exists"

# ── 2. Extract authority_source paths and check existence ──
# CONTRACT_INDEX.yaml is actually JSON format, parse with python3
if command -v python3 &>/dev/null; then
    AUTHORITY_SOURCES=$(python3 -c "
import json, sys
with open('$CONTRACT_INDEX') as f:
    data = json.load(f)
for c in data.get('contracts', []):
    src = c.get('authority_source', '')
    if src:
        print(src)
" 2>/dev/null)

    if [[ $? -ne 0 ]]; then
        fail "Failed to parse CONTRACT_INDEX.yaml as JSON"
        echo "Result: FAIL (1 check failed)"
        exit 1
    fi

    while IFS= read -r src; do
        CHECKED=$((CHECKED + 1))
        full_path="$PROJECT_ROOT/$src"
        if [[ -f "$full_path" ]]; then
            ok "authority_source exists: $src"
        else
            fail "authority_source MISSING: $src"
            MISSING+=("$src")
        fi
    done <<< "$AUTHORITY_SOURCES"

    # ── 3. Extract generated paths and check existence ──
    GENERATED_SOURCES=$(python3 -c "
import json
with open('$CONTRACT_INDEX') as f:
    data = json.load(f)
for c in data.get('contracts', []):
    for g in c.get('generated', []):
        if g:
            print(g)
" 2>/dev/null)

    while IFS= read -r gen; do
        CHECKED=$((CHECKED + 1))
        full_path="$PROJECT_ROOT/$gen"
        if [[ -f "$full_path" ]]; then
            ok "generated artifact exists: $gen"
        else
            # Generated artifacts may not exist yet — warn but don't fail
            info "generated artifact not yet built: $gen"
        fi
    done <<< "$GENERATED_SOURCES"

    # ── 4. Validate JSON syntax of contract files ──
    while IFS= read -r src; do
        full_path="$PROJECT_ROOT/$src"
        if [[ -f "$full_path" ]]; then
            case "$src" in
                *.json)
                    if python3 -c "import json; json.load(open('$full_path'))" 2>/dev/null; then
                        ok "Valid JSON: $src"
                    else
                        fail "Invalid JSON: $src"
                    fi
                    ;;
                *.yaml|*.yml)
                    if python3 -c "import yaml; yaml.safe_load(open('$full_path'))" 2>/dev/null; then
                        ok "Valid YAML: $src"
                    else
                        # Try as JSON since CONTRACT_INDEX.yaml is actually JSON
                        if python3 -c "import json; json.load(open('$full_path'))" 2>/dev/null; then
                            ok "Valid JSON (yaml extension): $src"
                        else
                            fail "Invalid YAML/JSON: $src"
                        fi
                    fi
                    ;;
                *.dmn)
                    # DMN files are XML — basic well-formedness check
                    if command -v xmllint &>/dev/null; then
                        if xmllint --noout "$full_path" 2>/dev/null; then
                            ok "Valid XML (DMN): $src"
                        else
                            fail "Invalid XML (DMN): $src"
                        fi
                    else
                        info "xmllint not available, skipping XML validation: $src"
                    fi
                    ;;
                *.ttl)
                    # Turtle files — just check non-empty
                    if [[ -s "$full_path" ]]; then
                        ok "Non-empty Turtle: $src"
                    else
                        fail "Empty Turtle file: $src"
                    fi
                    ;;
                *)
                    info "Skipping validation for unknown format: $src"
                    ;;
            esac
        fi
    done <<< "$AUTHORITY_SOURCES"

else
    fail "python3 not available — cannot parse CONTRACT_INDEX.yaml"
fi

# ── Summary ──
echo ""
echo "═══════════════════════════════════════"
echo "Contract Verification Summary"
echo "  Files checked: $CHECKED"
echo "  Missing files: ${#MISSING[@]}"
if [[ ${#MISSING[@]} -gt 0 ]]; then
    echo "  Missing:"
    for m in "${MISSING[@]}"; do
        echo "    - $m"
    done
fi
echo "═══════════════════════════════════════"

if [[ $FAILED -ne 0 ]]; then
    echo "Result: FAIL"
    exit 1
else
    echo "Result: PASS"
    exit 0
fi
