#!/usr/bin/env python3
"""GITS-KNE 客户经理持续经营流程图生成器：数据驱动 + 连线端点几何验证。
每条主链连线 = 前节点底部边界 → 后节点顶部边界；闭环虚线端点必须落在节点内。
"""
from __future__ import annotations

# 主链节点：按顺序定义 (编号, 名称, 副文本/None, 高, 类型)
# 类型: step 白底 | gate 橙色(人工门禁) | out 灰底(产物)
# y 由生成器自动推算（4px 网格，节点间距 24px）
GAP = 24

STAGE = [
    ("一 · 经营触发", 60, 220),
    ("二 · 访前准备", 316, 368),
    ("三 · 会中互动", 720, 432),
    ("四 · 离场承诺", 1188, 316),
    ("五 · 访后与持续经营", 1540, 320),
]

NODES = [
    # 阶段一
    ("① 市场慧眼 · 经营触发", "外部事件·交易异动·承诺到期扫描", 44, "step"),
    ("产物: OpportunitySignal × N → 今日经营队列", None, 32, "out"),
    ("② 客户洞察 · Customer Operating View", "事实 / 信号 / 未闭环承诺 / KYC 缺口", 40, "step"),
    # 阶段二
    ("③ KYC Gap · 访前准备", "KYC 冲突检测 · 缺口清单", 44, "step"),
    ("④ 访前 Mission · R1 报告", None, 32, "step"),
    ("⑤ 60 秒作战卡 · R2", None, 32, "step"),
    ("HG-C01 人工确认 → 才能进入会面", None, 32, "gate"),
    ("⑥ 产品适配 · 客户触达", None, 32, "step"),
    ("HG-D01 产品推荐 · HG-A01 触达消息确认", None, 32, "gate"),
    # 阶段三
    ("⑦ 会中 Interaction · 事实抽取", "Claim / Intent / Need / Concern", 44, "step"),
    ("产物: Candidate Claim · 录音/笔记/口述证据", None, 32, "out"),
    ("⑧ Fact Reconciliation · 事实对账", None, 32, "step"),
    ("DMN 决策: VERIFIED / CONFLICT / CANDIDATE", None, 32, "gate"),
    ("⑨ 会中助手 · 实时追问建议", None, 32, "step"),
    ("⑩ 离场确认", None, 32, "step"),
    ("HG-B02 对账确认 · HG-E01 离场确认", None, 32, "gate"),
    # 阶段四
    ("⑪ Commitment · 双方承诺", "客户承诺 ≠ 银行承诺", 44, "step"),
    ("⑫ Task · 任务派发", None, 32, "step"),
    ("⑬ 访后分析 · R4 内部正式报告", None, 32, "step"),
    ("⑭ R5-A CRM 短版 · R5-B Fact Pack", None, 32, "step"),
    ("HG-C02 报告审批", None, 32, "gate"),
    # 阶段五
    ("⑮ CRM 受控写回", "逐条审批 · 无门禁不执行副作用", 44, "step"),
    ("HG-F01 CRM 写回确认", None, 32, "gate"),
    ("⑯ 专业协同 · 产品/风险/合规", None, 32, "step"),
    ("⑰ R7 · R8 · 新证据驱动下一轮", None, 32, "step"),
    ("ClaimAssessment 更新 → 回到 ① 持续经营", None, 32, "gate"),
]

# 节点在图上 x=440 w=320，中心 600
X0, W = 440, 320
CX = X0 + W // 2  # 600
X1 = X0 + W       # 760

# 计算每个节点的 y（按阶段顺序，从上个阶段末尾 + GAP 续接）
# 阶段一从 zone 顶 + 32 开始
FIRST_Y = 92  # 阶段一 zone y=60，节点从 y=92 开始（标签下 32px）

ys = []
y = FIRST_Y
for h in [n[2] for n in NODES]:
    ys.append(y)
    y += h + GAP

# 记录每个节点底部
bottoms = [ys[i] + NODES[i][2] for i in range(len(NODES))]

# ---------------------------------------------------------------
# 几何验证
# ---------------------------------------------------------------
errors = []

def in_rect(x, y, rx, ry, rw, rh, eps=0.5):
    return (rx - eps <= x <= rx + rw + eps) and (ry - eps <= y <= ry + rh + eps)

# 1) 每对相邻节点的主链连线：源底 → 目标顶，x=CX
for i in range(len(NODES) - 1):
    src_bot = bottoms[i]
    dst_top = ys[i + 1]
    if src_bot > dst_top:
        errors.append(f"overlap node {i}->{i+1}: src_bot={src_bot} > dst_top={dst_top}")
    # 源端点应在源节点内（x=CX 在节点水平范围内）
    if not in_rect(CX, src_bot, X0, ys[i], W, NODES[i][2]):
        errors.append(f"conn {i}->{i+1} start ({CX},{src_bot}) not on node {i} rect({X0},{ys[i]},{W},{NODES[i][2]})")
    if not in_rect(CX, dst_top, X0, ys[i+1], W, NODES[i+1][2]):
        errors.append(f"conn {i}->{i+1} end ({CX},{dst_top}) not on node {i+1} rect({X0},{ys[i+1]},{W},{NODES[i+1][2]})")

# 2) 闭环虚线：起点=最后节点右边缘，终点=① 右边缘
last_bot = bottoms[-1]
loop_start = (X1, last_bot)          # (760, 最后节点底部)
loop_end = (X1, ys[0] + NODES[0][2] // 2)  # (760, ①节点中部)
if not in_rect(*loop_start, X0, ys[-1], W, NODES[-1][2]):
    errors.append(f"loop start {loop_start} not on last node rect({X0},{ys[-1]},{W},{NODES[-1][2]})")
if not in_rect(*loop_end, X0, ys[0], W, NODES[0][2]):
    errors.append(f"loop end {loop_end} not on node 0 rect({X0},{ys[0]},{W},{NODES[0][2]})")

# ---------------------------------------------------------------
if errors:
    print("FLOW GEOMETRY ERRORS:")
    for e in errors:
        print("  -", e)
    raise SystemExit(1)

print(f"FLOW GEOMETRY OK: {len(NODES)-1} main links + 1 loop verified; viewH={bottoms[-1]+GAP*4}")

# ---------------------------------------------------------------
# HTML 生成
# ---------------------------------------------------------------
FONT = "'Geist', 'PingFang SC', 'Microsoft YaHei', system-ui, sans-serif"
FONTM = "'Geist Mono', ui-monospace, 'PingFang SC', monospace"

def node_html(i):
    name, sub, h, kind = NODES[i]
    yy = ys[i]
    if kind == "gate":
        rect = f'<rect x="{X0}" y="{yy}" width="{W}" height="{h}" rx="6" fill="#eb6c36" fill-opacity="0.08" stroke="#eb6c36" stroke-width="1"/>'
        fill = "#2d3142"
        size = 10
    elif kind == "out":
        rect = f'<rect x="{X0}" y="{yy}" width="{W}" height="{h}" rx="6" fill="rgba(45,49,66,0.05)" stroke="#4f5d75" stroke-width="1"/>'
        fill = "#2d3142"
        size = 10
    else:
        rect = f'<rect x="{X0}" y="{yy}" width="{W}" height="{h}" rx="6" fill="#ffffff" stroke="#2d3142" stroke-width="1"/>'
        fill = "#2d3142"
        size = 12
    lines = [rect]
    if sub is None:
        # 单行文本，垂直居中
        mid = yy + h // 2 + 1
        lines.append(f'<text x="{CX}" y="{mid}" fill="{fill}" font-size="{size}" font-weight="600" font-family="{FONT}" text-anchor="middle">{name}</text>')
    else:
        # 两行：主名 + 副文本
        lines.append(f'<text x="{CX}" y="{yy + h//2 - 1}" fill="{fill}" font-size="{size}" font-weight="600" font-family="{FONT}" text-anchor="middle">{name}</text>')
        lines.append(f'<text x="{CX}" y="{yy + h - 9}" fill="#4f5d75" font-size="9" font-family="{FONTM}" text-anchor="middle">{sub}</text>')
    return "\n".join(lines)

def main():
    # zones
    zone_parts = []
    for label, zy, zh in STAGE:
        zone_parts.append(f'<rect x="80" y="{zy}" width="1040" height="{zh}" rx="8" fill="rgba(45,49,66,0.02)" stroke="rgba(45,49,66,0.10)" stroke-width="0.8"/>')
        lw = 128 if label.startswith("五") else 88
        zone_parts.append(f'<rect x="84" y="{zy+4}" width="{lw}" height="12" rx="2" fill="#f5f5f5"/>')
        zone_parts.append(f'<text x="{84+lw//2}" y="{zy+14}" fill="rgba(45,49,66,0.40)" font-size="7" font-family="{FONTM}" text-anchor="middle" letter-spacing="0.14em">{label}</text>')

    # connectors
    conn_parts = []
    for i in range(len(NODES) - 1):
        conn_parts.append(f'<path d="M {CX},{bottoms[i]} V {ys[i+1]}" fill="none" stroke="#4f5d75" stroke-width="1.2" marker-end="url(#arrow)"/>')
    # loop (blue dashed)
    conn_parts.append(f'<path d="M {X1},{bottoms[-1]} H 1100 V {loop_end[1]} H {X1}" fill="none" stroke="#2e5aa8" stroke-width="1" stroke-dasharray="5,4" marker-end="url(#arrow-link)"/>')
    ly = (bottoms[-1] + loop_end[1]) // 2
    conn_parts.append(f'<rect x="1012" y="{ly-6}" width="88" height="12" rx="2" fill="#f5f5f5"/>')
    conn_parts.append(f'<text x="1056" y="{ly+3}" fill="#2e5aa8" font-size="8" font-family="{FONTM}" text-anchor="middle" letter-spacing="0.06em">新证据驱动</text>')

    # nodes
    node_parts = [node_html(i) for i in range(len(NODES))]

    # legend
    legend_y = bottoms[-1] + 36
    legend_parts = [
        f'<line x1="120" y1="{legend_y}" x2="1080" y2="{legend_y}" stroke="rgba(45,49,66,0.10)" stroke-width="0.8"/>',
        f'<text x="120" y="{legend_y+8}" fill="#4f5d75" font-size="8" font-family="{FONTM}" letter-spacing="0.14em">LEGEND</text>',
        f'<text x="340" y="{legend_y+8}" fill="#4f5d75" font-size="8" font-family="{FONTM}">实线 = 主链推进 · 虚线 = 持续经营闭环 · 橙色 = 人工门禁(Human Gate)</text>',
    ]

    view_h = bottoms[-1] + 80

    html = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>客户经理持续经营业务主链流程</title>
  <link href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Geist:wght@400;500;600&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
  <style>
    *, *::before, *::after {{ box-sizing: border-box; margin: 0; padding: 0; }}
    :root {{
      --color-paper: #f5f5f5; --color-ink: #2d3142; --color-muted: #4f5d75; --color-accent: #eb6c36;
      --font-sans: 'Geist', 'PingFang SC', 'Microsoft YaHei', system-ui, sans-serif;
      --font-serif: 'Instrument Serif', 'Noto Serif CJK SC', serif;
      --font-mono: 'Geist Mono', ui-monospace, 'SF Mono', Menlo, monospace;
    }}
    body {{ font-family: var(--font-sans); background: var(--color-paper); color: var(--color-ink); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 3rem 2rem; }}
    .frame {{ max-width: 1280px; width: 100%; }}
    .eyebrow {{ font-family: var(--font-mono); font-size: 0.66rem; font-weight: 500; letter-spacing: 0.18em; text-transform: uppercase; color: var(--color-muted); margin-bottom: 0.5rem; }}
    h1 {{ font-family: var(--font-serif); font-size: clamp(1.5rem, 2.4vw + 0.75rem, 2rem); font-weight: 400; letter-spacing: -0.02em; line-height: 1.15; color: var(--color-ink); margin-bottom: 0.5rem; }}
    .subtitle {{ color: var(--color-muted); font-size: 0.9rem; margin-bottom: 1.5rem; }}
    svg {{ width: 100%; min-width: 1080px; display: block; }}
  </style>
</head>
<body>
  <div class="frame">
    <p class="eyebrow">Flow · Customer Engagement Loop</p>
    <h1>客户经理持续经营 — 业务主链闭环</h1>
    <p class="subtitle">市场慧眼 → 洞察 → 访前 → 会中 → 对账 → 承诺 → 访后 → 回写 → 持续经营</p>
    <svg viewBox="0 0 1200 {view_h}" xmlns="http://www.w3.org/2000/svg" role="img" aria-labelledby="gits-flow-title gits-flow-desc">
      <title id="gits-flow-title">客户经理持续经营业务主链流程</title>
      <desc id="gits-flow-desc">从市场慧眼经营触发到客户洞察、访前准备、会中互动、事实对账、离场承诺、访后分析与 CRM 受控回写，再到新证据驱动下一轮持续经营的完整业务闭环。</desc>
      <defs>
        <marker id="arrow" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#4f5d75"/></marker>
        <marker id="arrow-accent" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#eb6c36"/></marker>
        <marker id="arrow-link" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto"><polygon points="0 0, 8 3, 0 6" fill="#2e5aa8"/></marker>
      </defs>
      <rect width="100%" height="100%" fill="#f5f5f5"/>
      {chr(10).join(zone_parts)}
      {chr(10).join(conn_parts)}
      {chr(10).join(node_parts)}
      {chr(10).join(legend_parts)}
    </svg>
  </div>
</body>
</html>
"""
    with open("diagrams/flow.html", "w", encoding="utf-8") as f:
        f.write(html)
    print(f"WROTE diagrams/flow.html (viewH={view_h})")

if __name__ == "__main__":
    main()
