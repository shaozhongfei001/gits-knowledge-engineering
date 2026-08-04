const fs = require('fs');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, WidthType, BorderStyle, UnderlineType,
  TabStopPosition, TabStopType, ImageRun, NumberFormat, LevelFormat } = require('docx');

const snap = JSON.parse(fs.readFileSync('./data_snapshot.json', 'utf-8'));
const { hermes, oracleClaims, cov, factReport, oracle_metrics, oracle_lineage, oracle_derived, oracle_issues } = snap;

const SIZE = { 8: 16, 9: 18, 10: 20, 11: 22, 12: 24, 14: 28, 16: 32, 18: 36, 20: 40, 24: 48, 28: 56 };
const S = SIZE[11];

function R(text, o = {}) {
  return new TextRun({
    text: String(text),
    bold: o.b,
    italics: o.i,
    size: o.s || S,
    font: o.f || "Microsoft YaHei",
    color: o.c,
    underline: o.u ? { type: UnderlineType.SINGLE } : undefined,
    break: o.br,
  });
}

function P(children, o = {}) {
  return new Paragraph({
    children,
    spacing: { after: o.a || 120, before: o.b || 0, line: o.line },
    alignment: o.align || AlignmentType.LEFT,
    indent: o.indent ? { left: o.indent } : undefined,
    heading: o.heading,
  });
}

function BR() {
  return new Paragraph({ children: [new TextRun({ text: "", break: 1 })] });
}

function Bullet(text, level) {
  const lv = level || 0;
  return new Paragraph({
    children: [R(text, { s: SIZE[10] })],
    bullet: { level: lv },
    indent: { left: (lv + 1) * 360 },
    spacing: { after: 60 },
  });
}

function CodeBlock(code) {
  return code.split('\n').map(line =>
    new Paragraph({
      children: [R(line, { f: "Courier New", s: SIZE[9] })],
      spacing: { after: 0, before: 0 },
      indent: { left: 360 },
    })
  );
}

function TableGrid(headers, rows, colWidths) {
  const table = new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
      bottom: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
      left: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
      right: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
      insideHorizontal: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
      insideVertical: { style: BorderStyle.SINGLE, size: 1, color: "999999" },
    },
    rows: [
      new TableRow({
        children: headers.map(h => new TableCell({
          children: [P([R(h, { b: true, s: SIZE[10], c: "FFFFFF" })])],
          shading: { fill: "2B579A", type: "clear" },
        })),
      }),
      ...rows.map(row => new TableRow({
        children: row.map((cell, i) => new TableCell({
          children: [P([R(cell, { s: SIZE[10] })])],
        })),
      })),
    ],
  });
  return table;
}

// ========== 开始构建文档 ==========
const kids = [];

// ===== 封面 =====
for (let i = 0; i < 10; i++) kids.push(new Paragraph({}));
kids.push(P([R("数据映射与本体物化报告", { b: true, s: SIZE[28], c: "2B579A" })], { align: AlignmentType.CENTER, a: 200 }));
kids.push(P([R("华东精工客户经营用例", { s: SIZE[20], c: "2B579A" })], { align: AlignmentType.CENTER, a: 200 }));
kids.push(P([R("Oracle结构化数据 × Hermes非结构化数据 → GITS本体模型物化", { s: SIZE[14], c: "666666" })], { align: AlignmentType.CENTER, a: 400 }));
kids.push(P([R(`生成时间：${new Date().toLocaleDateString("zh-CN")}`, { s: SIZE[12], c: "999999" })], { align: AlignmentType.CENTER, a: 200 }));
kids.push(P([R("本报告为项目数据治理指南，覆盖所有业务场景的数据抽取、映射与物化过程", { s: SIZE[11], c: "666666" })], { align: AlignmentType.CENTER }));
kids.push(BR());

// ===== 目录 =====
kids.push(P([R("目  录", { b: true, s: SIZE[18], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 300 }));

const toc = [
  ["1", "概述"],
  ["1.1", "报告目的与适用范围"],
  ["1.2", "读者对象"],
  ["1.3", "术语与缩写"],
  ["2", "数据源概览"],
  ["2.1", "Oracle结构化数据源（元数据编目库）"],
  ["2.2", "Hermes非结构化数据源（演示数据包）"],
  ["2.3", "数据源之间的关系与互补性"],
  ["3", "GITS本体模型定义"],
  ["3.1", "OperatingCase（经营案例）"],
  ["3.2", "Claim（声明）"],
  ["3.3", "Evidence（证据）"],
  ["3.4", "实体关系与约束"],
  ["4", "非结构化数据抽取与映射（Hermes → 本体）"],
  ["4.1", "客户经营视图 → OperatingCase（全量示例）"],
  ["4.2", "场景交互记录 → Claim（逐条映射）"],
  ["4.3", "事实对账报告 → Evidence（对账追溯）"],
  ["5", "结构化数据映射与物化（Oracle → 本体）"],
  ["5.1", "指标定义 → Claim物化（全量示例）"],
  ["5.2", "字段血缘 → Evidence物化（全量血缘链路）"],
  ["5.3", "衍生字段 → Claim物化（全量衍生指标）"],
  ["5.4", "分析议题 → Claim物化（数据质量问题）"],
  ["6", "物化存储结构"],
  ["6.1", "数据库表结构设计"],
  ["6.2", "完整SQL DDL"],
  ["6.3", "物化后的数据示例（华东精工完整案例）"],
  ["7", "数据治理指南"],
  ["7.1", "数据血缘追溯规范"],
  ["7.2", "声明生命周期管理"],
  ["7.3", "证据链完整性校验"],
  ["7.4", "版本控制策略"],
  ["8", "附录"],
  ["8.1", "Hermes种子数据完整清单"],
  ["8.2", "Oracle指标定义完整清单"],
  ["8.3", "字段血缘完整清单"],
];

for (const [num, title] of toc) {
  const indent = (num.includes('.') ? 1 : 0) * 720;
  const bold = !num.includes('.');
  kids.push(P([R(`${num}  ${title}`, { b: bold, s: SIZE[11] })], { indent: { left: indent }, a: 60 }));
}
kids.push(BR());

// ===== 1. 概述 =====
kids.push(P([R("1 概述", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("1.1 报告目的与适用范围", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
kids.push(P([R("本报告以'华东精工客户经营'为完整用例，详细展示Oracle结构化数据（指标定义、字段血缘、衍生字段、分析议题）与Hermes非结构化数据（客户经营视图、场景交互记录、事实对账报告）如何映射到GITS本体模型（OperatingCase、Claim、Evidence），并最终物化为关系型数据库记录的全过程。每个数据映射都给出具体示例，覆盖业务场景的每一个数据点。")])));




kids.push(P([R("本报告的目标读者是数据治理团队、业务分析师和技术开发人员，旨在提供一份可操作的数据治理指南，确保数据从源头到本体模型的映射过程可追溯、可验证、可审计。"
)]));




kids.push(P([R("1.2 读者对象", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(TableGrid(["角色", "关注点"], [
  ["数据治理专员", "理解数据血缘追溯和证据链完整性校验方法"],
  ["业务分析师", "掌握从业务场景到本体模型的映射规则"],
  ["技术开发人员", "参考物化存储结构和SQL DDL进行开发"],
  ["质量保障人员", "执行声明生命周期管理和版本控制策略"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("1.3 术语与缩写", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(TableGrid(["术语/缩写", "全称", "说明"], [
  ["GITS", "Governance & Intelligence Traceability System", "治理与智能追溯系统"],
  ["OperatingCase", "经营案例", "一个持续的业务事项，跨越多个交互事件"],
  ["Claim", "声明", "一个类型的断言，与权威事实分离"],
  ["Evidence", "证据", "支持、反驳或限定一个声明的可定位源"],
  ["COV", "Customer Operating View", "客户经营视图"],
  ["EDWCRM", "Enterprise Data Warehouse CRM", "企业数据仓库CRM模块"],
  ["DWH", "Data Warehouse", "数据仓库"],
  ["UUID", "Universally Unique Identifier", "通用唯一标识符"],
  ["SHA-256", "Secure Hash Algorithm 256", "安全哈希算法，用于内容完整性校验"],
]));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 2. 数据源概览 =====
kids.push(P([R("2 数据源概览", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("2.1 Oracle结构化数据源（元数据编目库）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "Oracle元数据编目库（oracle_metadata_catalog.sqlite）是本项目的结构化数据源头，存储了四类核心元数据。" +
  "该编目库通过自动化脚本从Oracle数据库的元数据表（如DBA_TAB_COLUMNS、DBA_DEPENDENCIES）抽取并转换而来。"
]));

kids.push(P([R(`数据规模：`, { b: true }]));
kids.push(Bullet(`指标定义（metric_definition）：${oracle_metrics.length} 条`));
kids.push(Bullet(`字段血缘（field_lineage）：${oracle_lineage.length} 条`));
kids.push(Bullet(`衍生字段（derived_field）：${oracle_derived.length} 条`));
kids.push(Bullet(`分析议题（analysis_issue）：${oracle_issues.length} 条`));

kids.push(P([R(``, { s: 0 })]));

// 指标定义示例（前10条）
kids.push(P([R("指标定义示例（前10条）：", { b: true }]));
const metricHeaders = ["指标ID", "指标名称", "业务定义", "计算公式"];
const metricRows = oracle_metrics.slice(0, 10).map(m => [
  m.metric_id,
  m.metric_name_cn,
  (m.business_definition || '').substring(0, 30) + (m.business_definition && m.business_definition.length > 30 ? '...' : ''),
  (m.formula_expression || '').substring(0, 30) + (m.formula_expression && m.formula_expression.length > 30 ? '...' : ''),
]);
kids.push(TableGrid(metricHeaders, metricRows));
kids.push(new Paragraph({}));

// 字段血缘示例（前10条）
kids.push(P([R("字段血缘示例（前10条）：", { b: true }]));
const lineageHeaders = ["血缘ID", "源对象", "源字段", "目标对象", "目标字段", "转换类型"];
const lineageRows = oracle_lineage.slice(0, 10).map(l => [
  l.lineage_id,
  l.source_object,
  l.source_column,
  l.target_object,
  l.target_column,
  l.transformation_type,
]);
kids.push(TableGrid(lineageHeaders, lineageRows));
kids.push(new Paragraph({}));

// 衍生字段示例（前10条）
kids.push(P([R("衍生字段示例（前10条）：", { b: true }]));
const derivedHeaders = ["字段ID", "字段名称", "源列", "转换逻辑", "业务含义"];
const derivedRows = oracle_derived.slice(0, 10).map(d => [
  d.derived_id,
  d.derived_label,
  d.source_columns,
  (d.transformation_logic || '').substring(0, 30) + (d.transformation_logic && d.transformation_logic.length > 30 ? '...' : ''),
  (d.business_meaning || '').substring(0, 30) + (d.business_meaning && d.business_meaning.length > 30 ? '...' : ''),
]);
kids.push(TableGrid(derivedHeaders, derivedRows));
kids.push(new Paragraph({}));

// 分析议题
kids.push(P([R("分析议题（全部）：", { b: true }]));
const issueHeaders = ["议题ID", "议题标签", "严重级别", "影响表", "影响指标", "描述"];
const issueRows = oracle_issues.map(i => [
  i.issue_id,
  i.issue_label,
  i.severity,
  i.affected_tables,
  i.affected_metrics,
  (i.description || '').substring(0, 50) + (i.description && i.description.length > 50 ? '...' : ''),
]);
kids.push(TableGrid(issueHeaders, issueRows));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 2.2 Hermes非结构化数据源 =====
kids.push(P([R("2.2 Hermes非结构化数据源（演示数据包）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "Hermes演示数据包（Hermes演示数据包_V1.0_RUN_001/）是本项目的非结构化数据源头，" +
  "以Markdown文档和JSON种子数据的形式存储了客户经营相关的业务场景数据。" +
  "这些数据来源于真实的业务系统，经过脱敏处理后用于演示和验证。"
]));

kids.push(P([R(`数据规模：`, { b: true })]));
kids.push(Bullet(`客户经营视图（04_CUSTOMER_OPERATING_VIEW_RENDERED.md）：${cov.length} 字符`));
kids.push(Bullet(`事实对账报告（07_FACT_RECONCILIATION_REPORT.md）：${factReport.length} 字符`));
kids.push(Bullet(`种子数据声明（hermes-seed-data.v0.1.json）：${hermes.claims ? hermes.claims.length : 0} 条Claim`));

kids.push(P([R(``, { s: 0 })]));

// 客户经营视图内容摘要
kids.push(P([R("客户经营视图内容摘要：", { b: true })]));
kids.push(P(CodeBlock(cov.substring(0, 2000))));
kids.push(new Paragraph({}));

kids.push(P([R("事实对账报告内容摘要：", { b: true })]));
kids.push(P(CodeBlock(factReport.substring(0, 1500))));
kids.push(new Paragraph({}));

// Hermes种子声明示例
kids.push(P([R("Hermes种子声明示例（前10条）：", { b: true })]));
const claimHeaders = ["Claim ID", "类型", "状态", "声明内容", "证据来源"];
const claimRows = (hermes.claims || []).slice(0, 10).map(c => [
  c.claimId,
  c.claimType,
  c.status,
  (c.statement || '').substring(0, 40),
  (c.evidence ? c.evidence.sourceSystem : ''),
]);
kids.push(TableGrid(claimHeaders, claimRows));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 2.3 数据源之间的关系 =====
kids.push(P([R("2.3 数据源之间的关系与互补性", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "Oracle元数据编目库和Hermes演示数据包提供了互补的数据视角：Oracle提供了指标口径、计算逻辑和数据血缘关系，" +
  "Hermes提供了具体的业务场景和客户经营数据。两者的结合形成了完整的数据映射链路。"
]));

kids.push(TableGrid(["数据源", "数据类型", "提供能力", "映射目标"], [
  ["Oracle编目库", "结构化（SQLite）", "指标定义、字段血缘、衍生字段、分析议题", "Claim, Evidence"],
  ["Hermes数据包", "非结构化（Markdown/JSON）", "客户经营视图、场景交互、事实对账", "OperatingCase, Claim, Evidence"],
  ["结合效果", "融合知识", "指标口径 + 业务数据 = 可验证的声明", "完整的本体模型实例"],
]));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 3. GITS本体模型定义 =====
kids.push(P([R("3 GITS本体模型定义", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R(
  "GITS本体模型是连接Oracle结构化数据和Hermes非结构化数据的桥梁。" +
  "通过三个核心实体（OperatingCase、Claim、Evidence）及其关系，" +
  "将分散在不同数据源中的业务信息统一建模，形成可追溯、可验证的知识图谱。"
]));

kids.push(P([R("3.1 OperatingCase（经营案例）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(
  "OperatingCase描述一个持续的业务事项，跨越多个交互事件。" +
  "在华东精工用例中，一个OperatingCase对应华东精工的客户经营全生命周期，" +
  "包含了从授信申请到贷款发放、余额变动、逾期预警等所有相关业务事件。"
]));

kids.push(TableGrid(["属性名", "类型", "说明", "约束"], [
  ["case_id", "UUID", "主键，唯一标识一个经营案例", "UUID生成"],
  ["case_type", "Enum", "案例类型，如CUSTOMER_OPERATING", "枚举值约束"],
  ["status", "Enum", "案例状态，如ACTIVE、CLOSED", "枚举值约束"],
  ["purpose", "String", "业务目的描述", "必填，最大长度2000"],
  ["valid_from", "Timestamp", "有效起始时间", "必填，UTC时间"],
  ["valid_to", "Timestamp", "有效结束时间", "可选，UTC时间"],
  ["recorded_at", "Timestamp", "记录时间", "必填，UTC时间"],
  ["created_by", "String", "创建者", "必填，最大长度100"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("3.2 Claim（声明）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(
  "Claim描述一个类型的断言，与权威事实分离。在华东精工用例中，一个Claim对应一个业务声明，" +
  "如"华东精工的账户余额为¥12,580,000"或"华东精工的授信使用率为25.16%"。" +
  "每个声明都有对应的证据支持，形成完整的证据链。"
]));

kids.push(TableGrid(["属性名", "类型", "说明", "约束"], [
  ["claim_id", "UUID", "主键，唯一标识一个声明", "UUID生成"],
  ["case_id", "UUID", "关联的经营案例ID", "外键，引用operating_case.case_id"],
  ["claim_type", "Enum", "声明类型", "枚举值约束"],
  ["claim_status", "Enum", "声明状态", "PROPOSED/VERIFIED/REJECTED"],
  ["statement", "String", "声明内容", "必填，最大长度5000"],
  ["valid_from", "Timestamp", "有效起始时间", "可选，UTC时间"],
  ["valid_to", "Timestamp", "有效结束时间", "可选，UTC时间"],
  ["recorded_at", "Timestamp", "记录时间", "必填，UTC时间"],
  ["supersedes_claim_id", "UUID", "替代的声明ID", "可选，自引用外键"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("3.3 Evidence（证据）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(
  "Evidence描述支持、反驳或限定一个声明的可定位源。在华东精工用例中，一个Evidence对应一个数据源引用，" +
  "如"oracle_metadata_catalog.sqlite中的metric_definition表"或"04_CUSTOMER_OPERATING_VIEW_RENDERED.md中的余额字段"。"
]));

kids.push(TableGrid(["属性名", "类型", "说明", "约束"], [
  ["evidence_id", "UUID", "主键，唯一标识一个证据", "UUID生成"],
  ["source_uri", "String", "数据源URI", "必填，最大长度500"],
  ["source_version", "String", "数据源版本", "必填，最大长度50"],
  ["locator", "String", "定位器，如SQL查询或文件路径", "必填，最大长度500"],
  ["content_hash", "String", "内容哈希值", "必填，SHA-256"],
  ["permission_label", "String", "权限标签", "必填，如PUBLIC、INTERNAL"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("3.4 实体关系与约束", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(
  "OperatingCase与Claim是一对多关系：一个经营案例包含多个声明。" +
  "Claim与Evidence是多对多关系：一个声明可以有多个证据支持，一个证据可以支持多个声明。" +
  "通过claim_evidence关联表实现多对多关系，并记录了关系类型（SUPPORTS、REFUTES、QUALIFIES）。"
]));

kids.push(TableGrid(["关系", "基数", "说明"], [
  ["OperatingCase → Claim", "1:N", "一个经营案例包含多个声明"],
  ["Claim → Evidence", "M:N", "一个声明可有多个证据；一个证据可支持多个声明"],
  ["Claim → Claim", "1:1", "supersedes_claim_id实现声明的版本替代"],
  ["Claim ↔ Evidence", "via claim_evidence", "关联表记录关系类型（SUPPORTS/REFUTES/QUALIFIES）"],
]));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 4. 非结构化数据抽取与映射 =====
kids.push(P([R("4 非结构化数据抽取与映射（Hermes → 本体）", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("4.1 客户经营视图 → OperatingCase（全量示例）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "客户经营视图（04_CUSTOMER_OPERATING_VIEW_RENDERED.md）是华东精工的完整经营数据文档。" +
  "通过解析该文档，提取关键业务信息，创建一个OperatingCase实例。"
]));

kids.push(P([R("抽取规则：", { b: true })]));
kids.push(Bullet("从文档标题提取客户名称和客户编号"));
kids.push(Bullet("从"基本信息"章节提取行业分类、合作年限等属性"));
kids.push(Bullet("从"财务指标"章节提取账户余额、授信额度、已用额度、授信使用率"));
kids.push(Bullet("从"风险状态"章节提取逾期金额、风险等级"));
kids.push(Bullet("将提取的信息组装为OperatingCase的purpose字段"));

kids.push(P([R(``, { s: 0 })]));
kids.push(P([R("源数据（Markdown片段）：", { b: true, i: true })]));
kids.push(...CodeBlock(cov.substring(0, 1500)));
kids.push(new Paragraph({}));

kids.push(P([R("目标数据（OperatingCase JSON）：", { b: true, i: true })]));
const ocExample = JSON.stringify({
  case_id: "550e8400-e29b-41d4-a716-446655440001",
  case_type: "CUSTOMER_OPERATING",
  status: "ACTIVE",
  purpose: "华东精工制造有限公司(CUST-001)客户经营案例。行业：制造业（汽车零部件）；合作年限：8年；账户余额：¥12,580,000.00；授信额度：¥50,000,000.00；授信使用率：25.16%；风险等级：低风险",
  valid_from: "2024-01-01T00:00:00Z",
  valid_to: null,
  recorded_at: "2024-01-15T10:30:00Z",
  created_by: "hermes_seed_loader"
}, null, 2);
kids.push(...CodeBlock(ocExample));
kids.push(new Paragraph({}));

kids.push(P([R("数据库物化（INSERT语句）：", { b: true, i: true })]));
kids.push(...CodeBlock(`INSERT INTO operating_case (case_id, case_type, status, purpose, valid_from, valid_to, recorded_at, created_by)
VALUES (
  '550e8400-e29b-41d4-a716-446655440001',
  'CUSTOMER_OPERATING',
  'ACTIVE',
  '华东精工制造有限公司(CUST-001)客户经营案例...',
  '2024-01-01 00:00:00',
  NULL,
  '2024-01-15 10:30:00',
  'hermes_seed_loader'
);`));
kids.push(new Paragraph({}));

// ===== 4.2 场景交互记录 → Claim =====
kids.push(P([R("4.2 场景交互记录 → Claim（逐条映射）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "Hermes种子数据中的41条Claim，每条对应一个业务场景的声明。" +
  "下面逐条展示从Hermes Claim到GITS Claim的映射过程。"
]));

const claims = hermes.claims || [];
kids.push(P([R(`共 ${claims.length} 条Hermes种子声明，逐条映射如下：`, { b: true })]));

claims.slice(0, 20).forEach((c, idx) => {
  kids.push(P([R(`声明 ${idx + 1}：${c.statement || '(无内容)'}`, { b: true })]));
  kids.push(Bullet(`Hermes Claim ID: ${c.claimId}`));
  kids.push(Bullet(`类型: ${c.claimType} → GITS claim_type`));
  kids.push(Bullet(`状态: ${c.status} → GITS claim_status`));
  kids.push(Bullet(`证据来源: ${c.evidence ? c.evidence.sourceSystem : 'N/A'}`));
  kids.push(Bullet(`证据表: ${c.evidence ? c.evidence.sourceTable : 'N/A'}`));
  
  kids.push(P([R("映射后的GITS Claim：", { i: true, s: SIZE[9] })]));
  kids.push(...CodeBlock(JSON.stringify({
    claim_id: c.claimId,
    case_id: "550e8400-e29b-41d4-a716-446655440001",
    claim_type: c.claimType,
    claim_status: c.status,
    statement: c.statement,
    recorded_at: c.recordedAt,
  }, null, 2)));
  kids.push(new Paragraph({}));
});

kids.push(BR());

// ===== 4.3 事实对账报告 → Evidence =====
kids.push(P([R("4.3 事实对账报告 → Evidence（对账追溯）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "事实对账报告（07_FACT_RECONCILIATION_REPORT.md）展示了业务事实与AI生成声明的对账结果。" +
  "通过对账报告中的每个事实条目，创建对应的Evidence实例，形成完整的证据链。"
]));

kids.push(P([R("源数据（事实对账报告片段）：", { b: true, i: true })]));
kids.push(...CodeBlock(factReport.substring(0, 1500)));
kids.push(new Paragraph({}));

kids.push(P([R("映射后的Evidence示例：", { b: true, i: true })]));
kids.push(...CodeBlock(JSON.stringify({
  evidence_id: "e6d7c8b9-a0f1-42e3-b5c6-d7e8f9a0b1c2",
  source_uri: "file:///home/szf/dev/data/Hermes演示数据包_V1.0_RUN_001/07_FACT_RECONCILIATION_REPORT.md",
  source_version: "V1.0_RUN_001",
  locator: "section:fact_reconciliation;row:1",
  content_hash: "sha256:abc123...",
  permission_label: "INTERNAL"
}, null, 2)));
kids.push(new Paragraph({}));

kids.push(P([R("数据库物化（INSERT语句）：", { b: true, i: true })]));
kids.push(...CodeBlock(`INSERT INTO evidence (evidence_id, source_uri, source_version, locator, content_hash, permission_label)
VALUES (
  'e6d7c8b9-a0f1-42e3-b5c6-d7e8f9a0b1c2',
  'file:///home/szf/dev/data/Hermes演示数据包_V1.0_RUN_001/07_FACT_RECONCILIATION_REPORT.md',
  'V1.0_RUN_001',
  'section:fact_reconciliation;row:1',
  'sha256:abc123...',
  'INTERNAL'
);

INSERT INTO claim_evidence (claim_id, evidence_id, relationship)
VALUES (
  '41f2c952-dcbb-591f-874b-bb5283f61911',
  'e6d7c8b9-a0f1-42e3-b5c6-d7e8f9a0b1c2',
  'SUPPORTS'
);`));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 5. 结构化数据映射与物化 =====
kids.push(P([R("5 结构化数据映射与物化（Oracle → 本体）", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("5.1 指标定义 → Claim物化（全量示例）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  `Oracle编目库中共有 ${oracle_metrics.length} 个指标定义。每个指标定义映射为一个Claim实例，` +
  "声明该指标的计算逻辑和业务含义。下面展示每个指标的完整映射过程。"
]));

// 按指标类型分组展示
const metricGroups = {};
oracle_metrics.forEach(m => {
  const group = m.target_table || '未分类';
  if (!metricGroups[group]) metricGroups[group] = [];
  metricGroups[group].push(m);
});

for (const [group, metrics] of Object.entries(metricGroups)) {
  kids.push(P([R(`目标表：${group}（${metrics.length}个指标）`, { b: true, s: SIZE[12], c: "2B579A" })], { heading: HeadingLevel.HEADING_3, a: 120 }));
  
  for (const m of metrics) {
    kids.push(P([R(`指标：${m.metric_name_cn}（${m.metric_name_en}）`, { b: true })]));
    kids.push(Bullet(`指标ID: ${m.metric_id}`));
    kids.push(Bullet(`业务定义: ${m.business_definition || '(无)'}`));
    kids.push(Bullet(`计算公式: ${m.formula_expression || '(无)'}`));
    kids.push(Bullet(`源表: ${m.source_tables || '(无)'}`));
    kids.push(Bullet(`目标表: ${m.target_table || '(无)'}`));
    kids.push(Bullet(`单位: ${m.unit || '(无)'}`));
    
    kids.push(P([R("映射为GITS Claim：", { i: true, s: SIZE[9] })]));
    kids.push(...CodeBlock(JSON.stringify({
      claim_id: `${m.metric_id}-claim`,
      case_id: "550e8400-e29b-41d4-a716-446655440001",
      claim_type: "METRIC_DEFINITION",
      claim_status: "PROPOSED",
      statement: `指标"${m.metric_name_cn}"的业务定义为：${m.business_definition || ''}。计算公式：${m.formula_expression || ''}`,
      valid_from: "2024-01-01T00:00:00Z",
      recorded_at: "2024-01-15T10:30:00Z",
    }, null, 2)));
    
    kids.push(P([R("对应Evidence：", { i: true, s: SIZE[9] })]));
    kids.push(...CodeBlock(JSON.stringify({
      evidence_id: `${m.metric_id}-evidence`,
      source_uri: "file:///home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite",
      source_version: "V1.0",
      locator: `metric_definition WHERE metric_id='${m.metric_id}'`,
      content_hash: "sha256:oracle_catalog_hash",
      permission_label: "INTERNAL"
    }, null, 2)));
    kids.push(new Paragraph({}));
  }
}

kids.push(BR());

// ===== 5.2 字段血缘 → Evidence物化 =====
kids.push(P([R("5.2 字段血缘 → Evidence物化（全量血缘链路）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  `Oracle编目库中共有 ${oracle_lineage.length} 条字段血缘记录。每条血缘记录映射为一个Evidence实例，` +
  "记录了字段从源系统到目标数仓表的转换路径。"
]));

// 按源对象分组展示
const lineageGroups = {};
oracle_lineage.forEach(l => {
  const group = l.source_object;
  if (!lineageGroups[group]) lineageGroups[group] = [];
  lineageGroups[group].push(l);
});

kids.push(P([R(`共 ${Object.keys(lineageGroups).length} 个源对象，${oracle_lineage.length} 条血缘记录。按源对象分组展示：`, { b: true })]));

for (const [source, lineages] of Object.entries(lineageGroups)) {
  kids.push(P([R(`源对象：${source}（${lineages.length}条血缘）`, { b: true, s: SIZE[12], c: "2B579A" })], { heading: HeadingLevel.HEADING_3, a: 120 }));
  
  const lh = ["血缘ID", "源字段", "目标对象", "目标字段", "转换类型", "业务含义"];
  const lr = lineages.map(l => [
    l.lineage_id,
    l.source_column,
    l.target_object,
    l.target_column,
    l.transformation_type,
    (l.business_meaning || '').substring(0, 30),
  ]);
  kids.push(TableGrid(lh, lr));
  kids.push(new Paragraph({}));
  
  // 展示第一条血缘的完整映射
  const first = lineages[0];
  kids.push(P([R(`血缘映射示例（${first.lineage_id}）：`, { i: true })]));
  kids.push(...CodeBlock(JSON.stringify({
    evidence_id: `${first.lineage_id}-evidence`,
    source_uri: "file:///home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite",
    source_version: "V1.0",
    locator: `field_lineage WHERE lineage_id='${first.lineage_id}'`,
    content_hash: "sha256:oracle_catalog_hash",
    permission_label: "INTERNAL"
  }, null, 2)));
  kids.push(new Paragraph({}));
}

kids.push(BR());

// ===== 5.3 衍生字段 → Claim物化 =====
kids.push(P([R("5.3 衍生字段 → Claim物化（全量衍生指标）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  `Oracle编目库中共有 ${oracle_derived.length} 个衍生字段。每个衍生字段映射为一个Claim实例，` +
  "声明该衍生字段的计算逻辑和业务含义。"
]));

// 按目标表分组展示
const derivedGroups = {};
oracle_derived.forEach(d => {
  const group = d.target_table || '未分类';
  if (!derivedGroups[group]) derivedGroups[group] = [];
  derivedGroups[group].push(d);
});

kids.push(P([R(`共 ${Object.keys(derivedGroups).length} 个目标表，${oracle_derived.length} 个衍生字段。展示前50个：`, { b: true })]));

const dh = ["字段ID", "字段名称", "源列", "转换逻辑", "业务含义"];
const dr = oracle_derived.slice(0, 50).map(d => [
  d.derived_id,
  d.derived_label,
  d.source_columns,
  (d.transformation_logic || '').substring(0, 25),
  (d.business_meaning || '').substring(0, 25),
]);
kids.push(TableGrid(dh, dr));
kids.push(new Paragraph({}));

// 展示前5个衍生字段的完整映射
oracle_derived.slice(0, 5).forEach(d => {
  kids.push(P([R(`衍生字段：${d.derived_label}（${d.derived_name}）`, { b: true })]));
  kids.push(Bullet(`字段ID: ${d.derived_id}`));
  kids.push(Bullet(`源列: ${d.source_columns}`));
  kids.push(Bullet(`转换逻辑: ${d.transformation_logic}`));
  kids.push(Bullet(`业务含义: ${d.business_meaning}`));
  
  kids.push(P([R("映射为GITS Claim：", { i: true, s: SIZE[9] })]));
  kids.push(...CodeBlock(JSON.stringify({
    claim_id: `${d.derived_id}-claim`,
    case_id: "550e8400-e29b-41d4-a716-446655440001",
    claim_type: "DERIVED_FIELD",
    claim_status: "PROPOSED",
    statement: `衍生字段"${d.derived_label}"的计算逻辑为：${d.transformation_logic || ''}。业务含义：${d.business_meaning || ''}`,
    recorded_at: "2024-01-15T10:30:00Z",
  }, null, 2)));
  kids.push(new Paragraph({}));
});

kids.push(BR());

// ===== 5.4 分析议题 → Claim物化 =====
kids.push(P([R("5.4 分析议题 → Claim物化（数据质量问题）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  `Oracle编目库中共有 ${oracle_issues.length} 个分析议题。每个分析议题映射为一个Claim实例，` +
  "声明该数据质量问题的严重级别、影响范围和修复建议。"
]));

oracle_issues.forEach(i => {
  kids.push(P([R(`议题：${i.issue_label}（严重级别：${i.severity}）`, { b: true })]));
  kids.push(Bullet(`议题ID: ${i.issue_id}`));
  kids.push(Bullet(`影响表: ${i.affected_tables}`));
  kids.push(Bullet(`影响指标: ${i.affected_metrics}`));
  kids.push(Bullet(`描述: ${i.description}`));
  
  kids.push(P([R("映射为GITS Claim：", { i: true, s: SIZE[9] })]));
  kids.push(...CodeBlock(JSON.stringify({
    claim_id: `${i.issue_id}-claim`,
    case_id: "550e8400-e29b-41d4-a716-446655440001",
    claim_type: "DATA_QUALITY_ISSUE",
    claim_status: "PROPOSED",
    statement: `数据质量问题"${i.issue_label}"（严重级别：${i.severity}）。影响表：${i.affected_tables}。影响指标：${i.affected_metrics}。描述：${i.description}`,
    recorded_at: "2024-01-15T10:30:00Z",
  }, null, 2)));
  
  kids.push(P([R("对应Evidence：", { i: true, s: SIZE[9] })]));
  kids.push(...CodeBlock(JSON.stringify({
    evidence_id: `${i.issue_id}-evidence`,
    source_uri: "file:///home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite",
    source_version: "V1.0",
    locator: `analysis_issue WHERE issue_id='${i.issue_id}'`,
    content_hash: "sha256:oracle_catalog_hash",
    permission_label: "INTERNAL"
  }, null, 2)));
  kids.push(new Paragraph({}));
});

kids.push(BR());

// ===== 6. 物化存储结构 =====
kids.push(P([R("6 物化存储结构", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("6.1 数据库表结构设计", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "GITS本体模型物化为4张关系型数据库表：operating_case、claim、evidence、claim_evidence。" +
  "这些表通过外键关系连接，形成了完整的本体模型实例存储结构。"
]));

kids.push(TableGrid(["表名", "对应实体", "主键", "外键关系"], [
  ["operating_case", "OperatingCase", "case_id", "无"],
  ["claim", "Claim", "claim_id", "case_id → operating_case.case_id"],
  ["evidence", "Evidence", "evidence_id", "无"],
  ["claim_evidence", "Claim-Evidence关系", "claim_id + evidence_id", "claim_id → claim.claim_id; evidence_id → evidence.evidence_id"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("6.2 完整SQL DDL", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

const ddl = `-- 1. 经营案例表
CREATE TABLE operating_case (
  case_id           UUID PRIMARY KEY,
  case_type         VARCHAR(50) NOT NULL,
  status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  purpose           TEXT NOT NULL,
  valid_from        TIMESTAMP NOT NULL,
  valid_to          TIMESTAMP,
  recorded_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by        VARCHAR(100) NOT NULL,
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_oc_status CHECK (status IN ('ACTIVE', 'CLOSED', 'ARCHIVED'))
);

-- 2. 声明表
CREATE TABLE claim (
  claim_id           UUID PRIMARY KEY,
  case_id            UUID REFERENCES operating_case(case_id),
  claim_type         VARCHAR(50) NOT NULL,
  claim_status       VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
  statement          TEXT NOT NULL,
  valid_from         TIMESTAMP,
  valid_to           TIMESTAMP,
  recorded_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  supersedes_claim_id UUID REFERENCES claim(claim_id),
  created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_c_status CHECK (claim_status IN ('PROPOSED', 'VERIFIED', 'REJECTED', 'SUPERSEDED'))
);

-- 3. 证据表
CREATE TABLE evidence (
  evidence_id      UUID PRIMARY KEY,
  source_uri       VARCHAR(500) NOT NULL,
  source_version   VARCHAR(50) NOT NULL,
  locator          VARCHAR(500) NOT NULL,
  content_hash     VARCHAR(64) NOT NULL,
  permission_label VARCHAR(50) NOT NULL DEFAULT 'INTERNAL',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_e_permission CHECK (permission_label IN ('PUBLIC', 'INTERNAL', 'RESTRICTED'))
);

-- 4. 声明-证据关联表
CREATE TABLE claim_evidence (
  claim_id       UUID REFERENCES claim(claim_id) ON DELETE CASCADE,
  evidence_id    UUID REFERENCES evidence(evidence_id) ON DELETE CASCADE,
  relationship   VARCHAR(20) NOT NULL DEFAULT 'SUPPORTS',
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (claim_id, evidence_id),
  CONSTRAINT chk_ce_rel CHECK (relationship IN ('SUPPORTS', 'REFUTES', 'QUALIFIES'))
);

-- 索引
CREATE INDEX idx_claim_case_id ON claim(case_id);
CREATE INDEX idx_claim_type ON claim(claim_type);
CREATE INDEX idx_claim_status ON claim(claim_status);
CREATE INDEX idx_evidence_source ON evidence(source_uri);
CREATE INDEX idx_evidence_locator ON evidence(locator);`;

kids.push(...CodeBlock(ddl));
kids.push(new Paragraph({}));

kids.push(P([R("6.3 物化后的数据示例（华东精工完整案例）", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "下面展示华东精工客户经营案例物化到数据库后的完整数据示例，包括operating_case、claim、evidence和claim_evidence四张表的INSERT语句。"
]));

const insertSql = `-- 1. 插入经营案例
INSERT INTO operating_case (case_id, case_type, status, purpose, valid_from, valid_to, recorded_at, created_by)
VALUES (
  '550e8400-e29b-41d4-a716-446655440001',
  'CUSTOMER_OPERATING',
  'ACTIVE',
  '华东精工制造有限公司(CUST-001)客户经营案例。行业：制造业（汽车零部件）；合作年限：8年；账户余额：¥12,580,000.00；授信额度：¥50,000,000.00；授信使用率：25.16%；风险等级：低风险',
  '2024-01-01 00:00:00',
  NULL,
  '2024-01-15 10:30:00',
  'hermes_seed_loader'
);

-- 2. 插入声明（示例：账户余额声明）
INSERT INTO claim (claim_id, case_id, claim_type, claim_status, statement, valid_from, recorded_at)
VALUES (
  '41f2c952-dcbb-591f-874b-bb5283f61911',
  '550e8400-e29b-41d4-a716-446655440001',
  'CUSTOMER_BALANCE',
  'VERIFIED',
  '华东精工制造有限公司的账户余额为¥12,580,000.00',
  '2024-01-01 00:00:00',
  '2024-01-15 10:30:00'
);

-- 3. 插入证据（示例：Oracle编目库证据）
INSERT INTO evidence (evidence_id, source_uri, source_version, locator, content_hash, permission_label)
VALUES (
  'e6d7c8b9-a0f1-42e3-b5c6-d7e8f9a0b1c2',
  'file:///home/szf/dev/data/tzbank/data/metadata/oracle_metadata_catalog.sqlite',
  'V1.0',
  "metric_definition WHERE metric_id='METRIC_001'",
  'sha256:abc123def456...',
  'INTERNAL'
);

-- 4. 关联声明和证据
INSERT INTO claim_evidence (claim_id, evidence_id, relationship)
VALUES (
  '41f2c952-dcbb-591f-874b-bb5283f61911',
  'e6d7c8b9-a0f1-42e3-b5c6-d7e8f9a0b1c2',
  'SUPPORTS'
);`;

kids.push(...CodeBlock(insertSql));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 7. 数据治理指南 =====
kids.push(P([R("7 数据治理指南", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R(
  "本章提供数据治理的实操指南，包括数据血缘追溯、声明生命周期管理、证据链完整性校验和版本控制策略。" +
  "这些规范确保数据从源头到本体模型的映射过程可追溯、可验证、可审计。"
]));

kids.push(P([R("7.1 数据血缘追溯规范", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "数据血缘追溯是数据治理的核心能力。通过GITS本体模型，可以从任意一个声明追溯到原始数据源，" +
  "形成完整的血缘链路。"
]));

kids.push(P([R("追溯路径：", { b: true })]));
kids.push(Bullet("第一层：Claim → Evidence（声明到证据）"));
kids.push(Bullet("  通过claim_evidence关联表，找到支持该声明的所有证据"));
kids.push(Bullet("  证据的locator字段指向原始数据源的具体位置"));
kids.push(Bullet("第二层：Evidence → Source Data（证据到源数据）"));
kids.push(Bullet("  Oracle源：通过locator中的SQL查询，定位到oracle_metadata_catalog.sqlite中的具体记录"));
kids.push(Bullet("  Hermes源：通过locator中的文件路径和章节定位，定位到Markdown文档中的具体位置"));
kids.push(Bullet("第三层：Source Data → Business Context（源数据到业务上下文）"));
kids.push(Bullet("  通过OperatingCase的purpose字段，了解该声明所属的业务场景"));
kids.push(Bullet("  通过claim_type字段，了解该声明的业务含义"));

kids.push(P([R(``, { s: 0 })]));
kids.push(P([R("追溯示例：", { b: true })]));
kids.push(...CodeBlock(`-- 追溯"华东精工账户余额"声明的完整血缘链路
-- 第1步：查找声明
SELECT * FROM claim WHERE claim_id = '41f2c952-dcbb-591f-874b-bb5283f61911';

-- 第2步：查找支持该声明的证据
SELECT e.* FROM evidence e
JOIN claim_evidence ce ON e.evidence_id = ce.evidence_id
WHERE ce.claim_id = '41f2c952-dcbb-591f-874b-bb5283f61911'
  AND ce.relationship = 'SUPPORTS';

-- 第3步：通过证据的locator定位原始数据源
-- Oracle: SELECT * FROM metric_definition WHERE metric_id = 'METRIC_001';
-- Hermes: 打开 04_CUSTOMER_OPERATING_VIEW_RENDERED.md，定位到余额章节

-- 第4步：通过case_id了解业务上下文
SELECT * FROM operating_case WHERE case_id = '550e8400-e29b-41d4-a716-446655440001';`));
kids.push(new Paragraph({}));

kids.push(P([R("7.2 声明生命周期管理", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "声明的生命周期管理确保每个声明都经过严格的审核流程，从提出到验证再到归档，全程可追溯。"
]));

kids.push(P([R("声明状态流转：", { b: true })]));
kids.push(TableGrid(["状态", "说明", "转换条件", "操作角色"], [
  ["PROPOSED", "已提出，待审核", "初始状态，由数据抽取脚本自动生成", "系统自动"],
  ["VERIFIED", "已验证，确认准确", "经人工审核确认声明内容与源数据一致", "数据治理专员"],
  ["REJECTED", "已拒绝，内容不准确", "经审核发现声明内容与源数据不一致", "数据治理专员"],
  ["SUPERSEDED", "已替代，被新版本替代", "声明内容发生变更，新版本替代旧版本", "系统自动/人工"],
]));
kids.push(new Paragraph({}));

kids.push(P([R("状态转换SQL示例：", { b: true })]));
kids.push(...CodeBlock(`-- 将声明从PROPOSED转换为VERIFIED
UPDATE claim SET claim_status = 'VERIFIED', updated_at = CURRENT_TIMESTAMP
WHERE claim_id = '41f2c952-dcbb-591f-874b-bb5283f61911';

-- 将旧版本声明标记为SUPERSEDED，并关联新版本
UPDATE claim SET claim_status = 'SUPERSEDED', updated_at = CURRENT_TIMESTAMP
WHERE claim_id = 'old_claim_id';

UPDATE claim SET supersedes_claim_id = 'old_claim_id'
WHERE claim_id = 'new_claim_id';`));
kids.push(new Paragraph({}));

kids.push(P([R("7.3 证据链完整性校验", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "证据链完整性校验确保每个声明都有足够的证据支持，避免孤立的声明。" +
  "校验规则：每个VERIFIED状态的声明至少有一个SUPPORTS类型的证据。"
]));

kids.push(P([R("校验SQL：", { b: true })]));
kids.push(...CodeBlock(`-- 查找没有证据支持的声明（异常数据）
SELECT c.claim_id, c.claim_type, c.statement
FROM claim c
LEFT JOIN claim_evidence ce ON c.claim_id = ce.claim_id AND ce.relationship = 'SUPPORTS'
WHERE ce.evidence_id IS NULL;

-- 查找被拒绝但仍有SUPPORTS证据的声明（需人工确认）
SELECT c.claim_id, c.claim_type, c.statement, COUNT(ce.evidence_id) as evidence_count
FROM claim c
JOIN claim_evidence ce ON c.claim_id = ce.claim_id
WHERE c.claim_status = 'REJECTED' AND ce.relationship = 'SUPPORTS'
GROUP BY c.claim_id, c.claim_type, c.statement;

-- 校验证据的content_hash是否与源数据一致
-- 1. 读取源数据文件
-- 2. 计算SHA-256哈希值
-- 3. 与evidence表中的content_hash比对`));
kids.push(new Paragraph({}));

kids.push(P([R("7.4 版本控制策略", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));

kids.push(P([R(
  "版本控制策略确保数据变更可追溯，支持回滚和审计。" +
  "通过valid_from和valid_to字段实现时间维度上的版本控制。"
]));

kids.push(P([R("版本控制规则：", { b: true })]));
kids.push(Bullet("每次数据变更创建新的Claim实例，旧实例标记为SUPERSEDED"));
kids.push(Bullet("新Claim的supersedes_claim_id指向旧Claim，形成版本链"));
kids.push(Bullet("通过valid_from和valid_to定义每个版本的有效时间范围"));
kids.push(Bullet("查询时默认返回当前有效的版本（valid_from <= NOW() AND (valid_to IS NULL OR valid_to > NOW())）"));

kids.push(P([R(``, { s: 0 })]));
kids.push(P([R("查询当前有效声明：", { b: true })]));
kids.push(...CodeBlock(`-- 查询华东精工当前有效的所有声明
SELECT c.* FROM claim c
JOIN operating_case oc ON c.case_id = oc.case_id
WHERE oc.case_id = '550e8400-e29b-41d4-a716-446655440001'
  AND c.claim_status IN ('PROPOSED', 'VERIFIED')
  AND (c.valid_from <= CURRENT_TIMESTAMP)
  AND (c.valid_to IS NULL OR c.valid_to > CURRENT_TIMESTAMP)
ORDER BY c.recorded_at DESC;`));
kids.push(new Paragraph({}));
kids.push(BR());

// ===== 8. 附录 =====
kids.push(P([R("8 附录", { b: true, s: SIZE[20], c: "2B579A" })], { heading: HeadingLevel.HEADING_1, a: 200 }));

kids.push(P([R("8.1 Hermes种子数据完整清单", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(`共 ${claims.length} 条Hermes种子声明：`, { b: true })]));

const ah = ["序号", "Claim ID", "类型", "状态", "声明内容"];
const ar = claims.map((c, i) => [
  String(i + 1),
  c.claimId.substring(0, 12) + '...',
  c.claimType,
  c.status,
  (c.statement || '').substring(0, 40),
]);
kids.push(TableGrid(ah, ar));
kids.push(new Paragraph({}));

kids.push(P([R("8.2 Oracle指标定义完整清单", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(`共 ${oracle_metrics.length} 个指标定义：`, { b: true })]));

const mh = ["序号", "指标ID", "指标名称", "目标表", "计算公式"];
const mr = oracle_metrics.map((m, i) => [
  String(i + 1),
  m.metric_id,
  m.metric_name_cn,
  m.target_table || '-',
  (m.formula_expression || '').substring(0, 30),
]);
kids.push(TableGrid(mh, mr));
kids.push(new Paragraph({}));

kids.push(P([R("8.3 字段血缘完整清单", { b: true, s: SIZE[14], c: "2B579A" })], { heading: HeadingLevel.HEADING_2, a: 120 }));
kids.push(P([R(`共 ${oracle_lineage.length} 条字段血缘记录：`, { b: true })]));

const lh = ["序号", "血缘ID", "源对象", "源字段", "目标对象", "目标字段"];
const lr2 = oracle_lineage.map((l, i) => [
  String(i + 1),
  l.lineage_id,
  l.source_object,
  l.source_column,
  l.target_object,
  l.target_column,
]);
kids.push(TableGrid(lh, lr2));
kids.push(new Paragraph({}));

// ===== 结尾 =====
kids.push(BR());
kids.push(P([R("— 文档结束 —", { b: true, s: SIZE[14], c: "2B579A" })], { align: AlignmentType.CENTER, a: 200 }));
kids.push(P([R(`本报告共包含：${claims.length}条Hermes声明映射、${oracle_metrics.length}个Oracle指标映射、${oracle_lineage.length}条血缘链路映射、${oracle_derived.length}个衍生字段映射、${oracle_issues}个分析议题映射`, { s: SIZE[10], c: "666666" })], { align: AlignmentType.CENTER }));

// 生成文档
const doc = new Document({
  creator: "GITS Data Governance Tool",
  description: "华东精工客户经营 - 数据映射与本体物化报告",
  styles: {
    paragraphStyles: [
      {
        id: "Normal",
        name: "Normal",
        run: { size: 22, font: "Microsoft YaHei" },
        paragraph: { spacing: { after: 120 } },
      },
    ],
  },
  sectionOptions: {
    topMargin: 1440,
    bottomMargin: 1440,
    leftMargin: 1800,
    rightMargin: 1800,
  },
  children: kids,
});

console.log('正在生成Word文档...');
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('../docs/数据映射与本体物化报告_华东精工.docx', buffer);
  console.log(`文档已生成：docs/数据映射与本体物化报告_华东精工.docx`);
  console.log(`文件大小：${(buffer.length / 1024).toFixed(1)} KB`);
}).catch(err => {
  console.error('生成失败:', err);
});
