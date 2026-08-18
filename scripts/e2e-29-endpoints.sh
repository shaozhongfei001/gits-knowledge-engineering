#!/usr/bin/env bash
# e2e-29-endpoints.sh — 验证派工单列出的 29 个剧情环节端点
# 用法: bash scripts/e2e-29-endpoints.sh [BASE_URL]
# BASE_URL 默认 http://localhost:8082
set -uo pipefail

BASE="${1:-http://localhost:8082}"
PASS=0; FAIL=0; FAILED_LIST=()

CUSTOMER_ID="CUST-CORP-0001"
CASE_ID="a1b2c3d4-e5f6-7890-abcd-ef0123456789"      # 种子经营案例
SIGNAL_ID_CONFIRM="f47ac10b-58cc-4372-a567-0e02b2c3d479"  # DETECTED 融资信号
SIGNAL_ID_DISMISS="550e8400-e29b-41d4-a716-446655440000"   # DISMISSED 关系变化信号
GATE_ID="hg-0005-d01-evidence"
EVIDENCE_ID="evidence-001"

# 动态旅程 ID（由 start 返回）
JOURNEY_ID=""
OPERATING_CASE_ID=""

check() {
  local desc="$1" url="$2" expect_code="$3" method="${4:-GET}" data="${5:-}" code
  if [ "$method" = "GET" ]; then
    code=$(curl -s -o /tmp/e2e-body.json -w "%{http_code}" "$url")
  elif [ "$method" = "PUT" ]; then
    code=$(curl -s -o /tmp/e2e-body.json -w "%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$url")
  else
    code=$(curl -s -o /tmp/e2e-body.json -w "%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
  fi
  if [[ "$code" =~ ^2 ]] || [[ "$expect_code" = "$code" ]]; then
    echo "  ✅ $desc [$code]"
    PASS=$((PASS+1))
  else
    echo "  ❌ $desc [got $code, want $expect_code]"
    echo "     $(head -c 200 /tmp/e2e-body.json)"
    FAIL=$((FAIL+1)); FAILED_LIST+=("$desc [$code]")
  fi
}

echo "=== 29 端点 E2E 验证 (BASE=$BASE) ==="

echo "[4/29] 旅程启动"
JOURNEY_RESP=$(curl -s -X POST "$BASE/api/v1/engagement/journey/start" -H "Content-Type: application/json" -d '{"customerId":"CUST-CORP-0001"}')
JOURNEY_ID=$(echo "$JOURNEY_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('journeyId',''))" 2>/dev/null)
OPERATING_CASE_ID=$(echo "$JOURNEY_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('operatingCaseId',''))" 2>/dev/null)
if [ -n "$JOURNEY_ID" ]; then
  echo "  ✅ 旅程启动 [201] journeyId=$JOURNEY_ID"
  PASS=$((PASS+1))
else
  echo "  ❌ 旅程启动 [响应无 journeyId]"
  FAIL=$((FAIL+1)); FAILED_LIST+=("旅程启动 [无journeyId]")
fi

# 若旅程启动失败，后续依赖旅程的端点无法验证
echo "[1/29] 客户列表"
check "客户列表" "$BASE/api/v1/engagement/customer?rmId=ALL" 200

echo "[2/29] 经营视图"
check "经营视图" "$BASE/api/v1/engagement/customer/$CUSTOMER_ID/operating-view" 200

echo "[3/29] KYC缺口画像"
check "KYC缺口画像" "$BASE/api/v1/engagement/kyc/$CUSTOMER_ID/gap-profile" 200

echo "[5/29] 访前准备"
check "访前准备" "$BASE/api/v1/engagement/journey/$JOURNEY_ID/previsit" 200 POST "{\"customerId\":\"$CUSTOMER_ID\",\"operatingCaseId\":\"$OPERATING_CASE_ID\",\"visitObjective\":\"客户回访\"}"

echo "[6/29] 外联脚本"
check "外联脚本" "$BASE/api/v1/engagement/journey/outreach-script" 200 POST "{\"customerId\":\"$CUSTOMER_ID\",\"rmId\":\"RM-HD-001\",\"operatingCaseId\":\"$OPERATING_CASE_ID\",\"journeyId\":\"$JOURNEY_ID\",\"channel\":\"PHONE\"}"

echo "[7/29] 会面脚本"
check "会面脚本" "$BASE/api/v1/engagement/journey/meeting-script" 200 POST "{\"customerId\":\"$CUSTOMER_ID\",\"rmId\":\"RM-HD-001\",\"operatingCaseId\":\"$OPERATING_CASE_ID\",\"journeyId\":\"$JOURNEY_ID\"}"

echo "[8/29] 访后复盘"
check "访后复盘" "$BASE/api/v1/engagement/journey/$JOURNEY_ID/postvisit" 200 POST "{\"customerId\":\"$CUSTOMER_ID\",\"operatingCaseId\":\"$OPERATING_CASE_ID\",\"rawTranscript\":\"客户表示对供应链金融产品有兴趣，建议安排后续对接\"}"

echo "[9/29] 证据迭代"
check "证据迭代" "$BASE/api/v1/engagement/journey/$JOURNEY_ID/new-evidence" 200 POST "{\"customerId\":\"$CUSTOMER_ID\",\"operatingCaseId\":\"$OPERATING_CASE_ID\",\"evidenceDescription\":\"新增项目备案文件\"}"

echo "[10/29] 机会信号"
check "机会信号" "$BASE/api/v1/engagement/signal/$CASE_ID" 200

echo "[11/29] 信号确认"
check "信号确认" "$BASE/api/v1/engagement/signal/$SIGNAL_ID_CONFIRM/confirm" 200 POST '{}'

echo "[12/29] 信号驳回"
check "信号驳回" "$BASE/api/v1/engagement/signal/$SIGNAL_ID_DISMISS/dismiss" 200 POST '{}'

echo "[13/29] 产品匹配"
check "产品匹配" "$BASE/api/v1/engagement/customer/$CUSTOMER_ID/product-matching" 200 POST '{}'

echo "[14/29] 旅程完成"
check "旅程完成" "$BASE/api/v1/engagement/journey/$JOURNEY_ID/complete" 200 POST '{}'

echo "[15/29] 人工门禁列表"
check "人工门禁列表" "$BASE/api/v1/human-gates" 200

echo "[16/29] 人工门禁详情"
check "人工门禁详情" "$BASE/api/v1/human-gates/$GATE_ID" 200

echo "[17/29] 人工门禁决策"
check "人工门禁决策" "$BASE/api/v1/human-gates/$GATE_ID/decide" 200 POST '{"decision":"REJECT","reason":"证据不完整","actorId":"P-RISK-001"}'

echo "[18/29] 承诺列表"
check "承诺列表" "$BASE/api/v1/commitments" 200

echo "[19/29] 承诺完成"
COMMIT_ID=$(curl -s "$BASE/api/v1/commitments" 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['commitmentId'] if isinstance(d,list) and d else '')" 2>/dev/null)
if [ -n "$COMMIT_ID" ]; then
  check "承诺完成" "$BASE/api/v1/commitments/$COMMIT_ID/status?status=FULFILLED" 200 PUT '{}'
else
  echo "  ⚠️ 承诺列表为空，跳过承诺完成"
  FAILED_LIST+=("承诺完成 [无承诺ID]")
fi

echo "[20/29] CRM回写列表"
check "CRM回写列表" "$BASE/api/v1/crm/writeback-commands" 200

echo "[21/29] CRM回写决策"
WRITEBACK_ID=$(curl -s "$BASE/api/v1/crm/writeback-commands" 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['commandId'] if isinstance(d,list) and d else '')" 2>/dev/null)
if [ -n "$WRITEBACK_ID" ]; then
  check "CRM回写决策" "$BASE/api/v1/crm/writeback-commands/$WRITEBACK_ID/decide" 200 POST '{"decision":"APPROVE","actorId":"P-CRM-001"}'
else
  echo "  ⚠️ CRM回写列表为空，跳过决策"
  FAILED_LIST+=("CRM回写决策 [无命令ID]")
fi

echo "[22/29] 审计追踪"
check "审计追踪" "$BASE/api/v1/audit-trace" 200

echo "[23/29] 证据版本"
check "证据版本" "$BASE/api/v1/evidences/$EVIDENCE_ID/versions" 200

echo "[24/29] 主张查询"
check "主张查询" "$BASE/api/claim/case/$CASE_ID" 200

echo "[25/29] 交易流水"
check "交易流水" "$BASE/api/v1/engagement/customer/$CUSTOMER_ID/transactions" 200

echo "[26/29] 外联脚本列表"
check "外联脚本列表" "$BASE/api/v1/engagement/journey/outreach-scripts?customerId=$CUSTOMER_ID" 200

echo "[27/29] 会面脚本列表"
check "会面脚本列表" "$BASE/api/v1/engagement/journey/meeting-scripts?customerId=$CUSTOMER_ID" 200

echo "[28/29] 交互记录"
check "交互记录" "$BASE/api/interaction?caseId=$CASE_ID" 200

echo "[29/29] 架构状态"
check "架构状态" "$BASE/api/v1/architecture/status" 200

echo ""
echo "=== 结果汇总 ==="
echo "  PASS: $PASS"
echo "  FAIL: $FAIL"
if [ ${#FAILED_LIST[@]} -gt 0 ]; then
  echo "  失败项:"
  for f in "${FAILED_LIST[@]}"; do echo "    - $f"; done
fi
echo "  EXIT=$( [ $FAIL -eq 0 ] && echo 0 || echo 1 )"
exit $FAIL
