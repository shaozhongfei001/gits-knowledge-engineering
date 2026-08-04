const fs = require('fs');
const path = require('path');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, 
  HeadingLevel, AlignmentType, TabStopPosition, TabStopType, 
  WidthType, BorderStyle, PageBreak } = require('docx');

// 加载数据
const snap = JSON.parse(fs.readFileSync('./data_snapshot.json', 'utf-8'));
const { hermes, oracleClaims, cov, factReport, oracle_metrics, oracle_lineage, oracle_derived, oracle_issues } = snap;

// 工具函数
function makeRun(text, opts = {}) {
  return new TextRun({
    text,
    bold: opts.bold || false,
    italics: opts.italics || false,
    size: opts.size || 22, // 22 = 11pt
    font: opts.font || "Microsoft YaHei",
    color: opts.color || undefined,
    underline: opts.underline ? { type: UnderlineType.SINGLE } : undefined,
  });
}

function makeCell(text, opts = {}) {
  const cell = new TableCell({
    children: [new Paragraph({
      children: [makeRun(text, { size: opts.size || 20 })],
    })],
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
  });
  return cell;
}

function makeHeaderCell(text) {
  const cell = new TableCell({
    children: [new Paragraph({
      children: [makeRun(text, { bold: true, size: 20, color: "FFFFFF" })],
    })],
  });
  return cell;
}

function makePara(text, opts = {}) {
  return new Paragraph({
    children: [makeRun(text, opts.run || {})],
    spacing: { after: opts.after || 120, before: opts.before || 0 },
    alignment: opts.align || AlignmentType.LEFT,
    indent: opts.indent ? { left: Twip.fromCm(opts.indent) } : undefined,
  });
}

function makeBullet(text, level = 1) {
  const indent = level * 360;
  return new Paragraph({
    children: [makeRun(text, { size: 20 })],
    bullet: { level: level - 1 },
    indent: { left: indent + 360 },
    spacing: { after: 60 },
  });
}

function makeCodeBlock(code) {
  const lines = code.split('\n');
  return lines.map(line => 
    new Paragraph({
      children: [new TextRun({ text: line, font: "Courier New", size: 18 })],
      spacing: { after: 0, before: 0 },
      indent: { left: 360 },
    })
  );
}

// 创建文档
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
  children: [],
});

const children = doc.children;

// ===== 封面 =====
for (let i = 0; i < 8; i++) children.push(new Paragraph({}));

children.push(new Paragraph({
  children: [makeRun("数据映射与本体物化报告", { size: 56, bold: true, color: "2B579A" })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 200 },
}));

children.push(new Paragraph({
  children: [makeRun("华东精工客户经营用例", { size: 40, color: "2B579A" })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 200 },
}));

children.push(new Paragraph({
  children: [makeRun("Oracle结构化数据 × Hermes非结构化数据 → GITS本体模型物化", { size: 26, color: "666666" })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 400 },
}));

children.push(new Paragraph({
  children: [makeRun(`生成时间：${new Date().toLocaleDateString("zh-CN")}`, { size: 24, color: "999999" })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 200 },
}));

children.push(new Paragraph({
  children: [makeRun("本报告为项目数据治理指南，覆盖所有业务场景的数据抽取、映射与物化过程", { size: 22, color: "666666" })],
  alignment: AlignmentType.CENTER,
  spacing: { after: 200 },
}));

// 分页
children.push(new Paragraph({ children: [new TextRun({ text: "", break: 1 })] }));

// ===== 目录 =====
children.push(new Paragraph({
  children: [makeRun("目录", { size: 36, bold: true, color: "2B579A" })],
  heading: HeadingLevel.HEADING_1,
  spacing: { after: 300 },
}));

const tocItems = [
  ["1", "概述", 0],
  ["1.1", "报告目的与适用范围", 0],
  ["1.2", "读者对象", 0],
  ["1.3", "术语与缩写", 0],
  ["2", "数据源概览", 0],
  ["2.1", "Oracle结构化数据源（元数据编目库）", 0],
  ["2.2", "Hermes非结构化数据源（演示数据包）", 0],
  ["2.3", "数据源之间的关系与互补性", 0],
  ["3", "GITS本体模型定义", 0],
  ["3.1", "OperatingCase（经营案例）", 0],
  ["3.2", "Claim（声明）", 0],
  ["3.3", "Evidence（证据）", 0],
  ["3.4", "实体关系与约束", 0],
  ["4", "非结构化数据抽取与映射（Hermes → 本体）", 0],
  ["4.1", "客户经营视图 → OperatingCase（全量示例）", 0],
  ["4.2", "场景交互记录 → Claim（逐条映射）", 0],
  ["4.3", "事实对账报告 → Evidence（对账追溯）", 0],
  ["5", "结构化数据映射与物化（Oracle → 本体）", 0],
  ["5.1", "指标定义 → Claim物化（175个指标全量示例）", 0],
  ["5.2", "字段血缘 → Evidence物化（910条血缘链路）", 0],
  ["5.3", "衍生字段 → Claim物化（1182个衍生指标）", 0],
  ["5.4", "分析议题 → Claim物化（4个数据质量问题）", 0],
  ["6", "物化存储结构", 0],
  ["6.1", "数据库表结构设计", 0],
  ["6.2", "完整SQL DDL", 0],
  ["6.3", "物化后的数据示例（华东精工完整案例）", 0],
  ["7", "数据治理指南", 0],
  ["7.1", "数据血缘追溯规范", 0],
  ["7.2", "声明生命周期管理", 0],
  ["7.3", "证据链完整性校验", 0],
  ["7.4", "版本控制策略", 0],
  ["8", "附录", 0],
  ["8.1", "Hermes种子数据完整清单", 0],
  ["8.2", "Oracle指标定义完整清单", 0],
  ["8.3", "字段血缘完整清单", 0],
];

for (const [num, title, level] of tocItems) {
  const indent = level * 720;
  const bold = level === 0;
  children.push(new Paragraph({
    children: [makeRun(`${num} ${title}`, { size: 22, bold })],
    indent: { left: indent },
    spacing: { after: 60 },
  }));
}

children.push(new Paragraph({ children: [new TextRun({ text: "", break: 1 })] }));

// Save part1
module.exports = { doc, children, snap, makeRun, makeCell, makeHeaderCell, makePara, makeBullet, makeCodeBlock, tocItems };
