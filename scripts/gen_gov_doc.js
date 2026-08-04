const fs = require("fs");
const path = require("path");
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, WidthType, BorderStyle } = require("docx");

const snap = JSON.parse(fs.readFileSync(path.join(__dirname, "data_snapshot.json"), "utf-8"));
const { cov, factReport, oracle_metrics, oracle_lineage, oracle_derived, oracle_issues } = snap;
const hermes = snap.hermes.claims || [];

const SZ = {8:16,9:18,10:20,11:22,12:24,14:28,16:32,18:36,20:40,24:48,28:56};
const S = SZ[11];

function R(t,o){o=o||{};return new TextRun({text:String(t),bold:o.b,italics:o.i,size:o.s||S,font:o.f||"Microsoft YaHei",color:o.c})}
function P(ch,o){o=o||{};return new Paragraph({children:ch||[],spacing:{after:o.a||120,before:o.b||0},alignment:o.al||AlignmentType.LEFT,heading:o.h})}
function BR(){return new Paragraph({children:[new TextRun({text:""})]})}
function B(t){return new Paragraph({children:[R(t,{s:SZ[10]})],bullet:{level:0},indent:{left:720},spacing:{after:60}})}
function CB(code){return code.split("\n").map(l=>new Paragraph({children:[R(l,{f:"Courier New",s:SZ[9]})],spacing:{after:0,before:0},indent:{left:360}}))}

function TG(headers,rows){
  return new Table({
    width:{size:100,type:WidthType.PERCENTAGE},
    borders:{
      top:{style:BorderStyle.SINGLE,size:1,color:"999999"},
      bottom:{style:BorderStyle.SINGLE,size:1,color:"999999"},
      left:{style:BorderStyle.SINGLE,size:1,color:"999999"},
      right:{style:BorderStyle.SINGLE,size:1,color:"999999"},
      insideHorizontal:{style:BorderStyle.SINGLE,size:1,color:"999999"},
      insideVertical:{style:BorderStyle.SINGLE,size:1,color:"999999"},
    },
    rows:[
      new TableRow({children:headers.map(h=>new TableCell({children:[P([R(h,{b:true,s:SZ[10],c:"FFFFFF"})])],shading:{fill:"2B579A",type:"clear"}}))}),
      ...rows.map((row,ri)=>new TableRow({children:row.map(c=>new TableCell({children:[P([R(c,{s:SZ[9]})])]})),shading:ri%2===0?undefined:{fill:"F5F5F5",type:"clear"}})),
    ],
  });
}

function esc(s){ return (s||"").toString().replace(/'/g,"''").slice(0,200); }

const k = [];

// === Cover ===
k.push(P([R("数据映射与本体物化报告")],{h:HeadingLevel.HEADING_1,al:AlignmentType.CENTER,a:400}));
k.push(P([R("华东精工客户经营用例")],{h:HeadingLevel.HEADING_1,al:AlignmentType.CENTER,a:200}));
k.push(P([R("Oracle结构化数据 x Hermes非结构化数据 -> GITS本体模型")],{al:AlignmentType.CENTER,a:100}));
k.push(BR(), BR());
k.push(P([R("版本：v1.0")],{al:AlignmentType.CENTER}));
k.push(P([R("日期：2026-08-04")],{al:AlignmentType.CENTER}));
k.push(P([R("状态：数据治理指南（草案）")],{al:AlignmentType.CENTER}));
k.push(BR(), BR());

// === TOC ===
k.push(P([R("目录")],{h:HeadingLevel.HEADING_1}));
const tocItems = [
  "1. 概述与目标",
  "2. GITS本体模型速览",
  "3. Hermes非结构化数据映射（逐条）",
  "4. Oracle结构化数据映射（逐条）",
  "5. 关系型数据库物化方案",
  "6. 数据治理规则与校验清单",
  "7. 附录：完整映射索引"
];
tocItems.forEach(t => k.push(P([R(t,{})],{h:HeadingLevel.HEADING_2})));
k.push(BR());

// === Chapter 1: Overview ===
k.push(P([R("1. 概述与目标")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("1.1 背景")],{h:HeadingLevel.HEADING_2}));
k.push(P([R("本报告的读者是GITS项目的数据治理人员、领域工程师和QA审计员。它回答了以下核心问题：")],{}));
k.push(B("GITS的本体模型（OperatingCase/Claim/Evidence）如何从Oracle结构化数据和Hermes非结构化数据中抽取并物化？"));
k.push(B("每一条映射关系对应的源数据是什么？目标字段是什么？转换规则是什么？"));
k.push(B("如何在关系型数据库（SQLite/PostgreSQL）中持久化这些本体实体？"));
k.push(BR());

k.push(P([R("1.2 数据源清单")],{h:HeadingLevel.HEADING_2}));
k.push(TG(["数据源","类型","记录数","示例字段"],[
  ["Oracle元数据编目库","结构化","metrics:"+oracle_metrics.length+" / lineage:"+oracle_lineage.length+" / derived:"+oracle_derived.length+" / issues:"+oracle_issues.length,"metric_name, source_table, transformation_logic"],
  ["Hermes种子Claim","非结构化",String(hermes.length),"claim_type, subject, evidence_refs"],
  ["客户经营视图(COV)","非结构化","—","客户画像、场景剧本"],
  ["事实对账报告","非结构化","—","claim_match, reconciliation_status"],
]));
k.push(BR());

k.push(P([R("1.3 本体模型实体")],{h:HeadingLevel.HEADING_2}));
k.push(TG(["实体","业务含义","对应源数据","关系表"],[
  ["OperatingCase","一个客户经营案例（如华东精工）","COV客户画像","operating_case"],
  ["Claim","关于客户的一条声明/断言","Hermes Claim / Oracle指标","claim"],
  ["Evidence","支撑声明的证据","Oracle血缘/衍生字段 / Hermes交互记录","evidence"],
]));
k.push(BR());

// === Chapter 2: Ontology Model ===
k.push(P([R("2. GITS本体模型速览")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("2.1 核心实体关系图")],{h:HeadingLevel.HEADING_2}));
k.push(P([R("OperatingCase (1) ----< (*) Claim ----> (*) Evidence")],{}));
k.push(B("一个OperatingCase可包含多个Claim"));
k.push(B("一个Claim可由多个Evidence支撑"));
k.push(B("Evidence可来自结构化源（Oracle）或非结构化源（Hermes）"));
k.push(BR());

k.push(P([R("2.2 关系型Schema")],{h:HeadingLevel.HEADING_2}));
k.push(...CB([
  "CREATE TABLE operating_case (",
  "  case_id TEXT PRIMARY KEY,",
  "  case_name TEXT NOT NULL,",
  "  industry TEXT,",
  "  region TEXT,",
  "  status TEXT DEFAULT 'ACTIVE',",
  "  created_at TEXT DEFAULT datetime('now')",
  ");",
  "",
  "CREATE TABLE claim (",
  "  claim_id TEXT PRIMARY KEY,",
  "  claim_type TEXT NOT NULL,",
  "  subject TEXT,",
  "  predicate TEXT,",
  "  object_value TEXT,",
  "  confidence_score REAL DEFAULT 0.5,",
  "  case_id TEXT REFERENCES operating_case(case_id),",
  "  source_system TEXT,",
  "  status TEXT DEFAULT 'PENDING',",
  "  created_at TEXT DEFAULT datetime('now')",
  ");",
  "",
  "CREATE TABLE evidence (",
  "  evidence_id TEXT PRIMARY KEY,",
  "  evidence_type TEXT NOT NULL,",
  "  source_system TEXT,",
  "  source_id TEXT,",
  "  claim_id TEXT REFERENCES claim(claim_id),",
  "  content TEXT,",
  "  metric_name TEXT,",
  "  transformation_logic TEXT,",
  "  status TEXT DEFAULT 'PENDING',",
  "  created_at TEXT DEFAULT datetime('now')",
  ");",
].join("\n")));
k.push(BR());

// === Chapter 3: Hermes Mapping ===
k.push(P([R("3. Hermes非结构化数据映射（逐条）")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("本章逐条展示Hermes种子数据中的Claim如何映射到GITS本体模型。")],{}));
k.push(BR());

hermes.forEach((cl, i) => {
  const num = i + 1;
  const ctype = cl.claimType || cl.type || "N/A";
  k.push(P([R("3." + num + " Claim: " + ctype)],{h:HeadingLevel.HEADING_3}));
  
  k.push(P([R("源数据（Hermes种子Claim #"+num+"）")],{}));
  k.push(...CB(JSON.stringify(cl, null, 2).slice(0, 600)));
  k.push(BR());
  
  k.push(P([R("映射说明")],{}));
  k.push(B("该Claim映射为一个Claim实体，同时其evidence_refs映射为Evidence实体"));
  k.push(B("OperatingCase通过case_id关联到华东精工案例"));
  k.push(BR());
  
  k.push(P([R("字段映射表")],{}));
  const headers = ["Hermes源字段", "GITS目标字段", "转换规则", "示例值"];
  const evidenceArr = cl.evidence || cl.evidenceRefs || [];
  const evidenceLen = Array.isArray(evidenceArr) ? evidenceArr.length : (evidenceArr ? 1 : 0);
  const rows = [
    ["claimId", "Claim.claim_id", "直接映射", cl.claimId || cl.id || "—"],
    ["claimType/type", "Claim.claim_type", "枚举映射", ctype],
    ["statement", "Claim.subject", "直接映射", cl.statement || cl.subject || "—"],
    ["predicate", "Claim.predicate", "直接映射", cl.predicate || "—"],
    ["object/value", "Claim.object_value", "直接映射", cl.object || cl.value || "—"],
    ["confidence", "Claim.confidence_score", "数值映射", String(cl.confidence != null ? cl.confidence : "—")],
    ["evidence/evidenceRefs", "Evidence.evidence_id", "展开为Evidence", String(evidenceLen) + "条"],
    ["caseId", "OperatingCase.case_id", "外键关联", cl.caseId || "CASE_EAST_PRECISION"],
  ];
  k.push(TG(headers, rows));
  k.push(BR());
  
  k.push(P([R("物化SQL示例")],{}));
  // evidence is an object, not an array
  const ev = cl.evidence || {};
  const evId = ev.interactionId || ev.source_id || "EVM_REF";
  let sql = "-- Insert Claim\n";
  sql += "INSERT INTO claim (claim_id, claim_type, subject, predicate, object_value, confidence_score, case_id, status, created_at)\n";
  sql += "VALUES ('CLM_"+num+"', '"+ctype+"', '"+esc(cl.statement||cl.subject)+"', ";
  sql += "'"+esc(cl.predicate)+"', '"+esc(cl.object||cl.value)+"', ";
  sql += (cl.confidence != null ? cl.confidence : 0.5) + ", 'CASE_EAST_PRECISION', 'VERIFIED', datetime('now'));\n\n";
  if (evId) {
    sql += "-- Insert Evidence\n";
    sql += "INSERT INTO evidence (evidence_id, evidence_type, source_system, source_id, claim_id, content, status, created_at)\n";
    sql += "VALUES ('EVM_"+num+"', 'UNSTRUCTURED', '"+esc(ev.sourceSystem)+"', '"+esc(evId)+"', 'CLM_"+num+"', ";
    sql += "'"+esc(JSON.stringify(ev))+"', 'VERIFIED', datetime('now'));\n";
  }
  k.push(...CB(sql));
  k.push(BR());
});

// === Chapter 4: Oracle Mapping ===
k.push(P([R("4. Oracle结构化数据映射（逐条）")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("本章展示Oracle元数据编目库中的指标定义、字段血缘、衍生字段和分析议题如何映射到GITS本体模型。")],{}));
k.push(BR());

k.push(P([R("4.1 指标定义 -> Claim")],{h:HeadingLevel.HEADING_2}));
k.push(P([R("Oracle的metric_definition表中的每条指标定义，映射为一个Claim实体，表示关于客户经营的一条量化声明。")],{}));
k.push(BR());

oracle_metrics.slice(0, 20).forEach((m, i) => {
  const num = i + 1;
  k.push(P([R("4.1." + num + " 指标: " + (m.metric_name_cn || m.metric_name_en || "N/A"))],{h:HeadingLevel.HEADING_3}));
  k.push(P([R("源数据（Oracle metric_definition #"+num+"）")],{}));
  k.push(...CB(JSON.stringify(m, null, 2).slice(0, 500)));
  k.push(BR());
  
  k.push(P([R("字段映射表")],{}));
  k.push(TG(["Oracle源字段", "GITS目标字段", "转换规则", "示例值"],[
    ["metric_id", "Claim.claim_id", "前缀转换 METRIC->CLM", m.metric_id || m.id || "—"],
    ["metric_name_cn", "Claim.subject", "直接映射", m.metric_name_cn || m.metric_name_en || "—"],
    ["metric_status", "Claim.claim_type", "枚举映射", m.metric_status || "—"],
    ["formula_expression", "Claim.predicate", "直接映射", (m.formula_expression || m.business_definition || "—").toString().slice(0,50)],
    ["unit", "Claim.object_value", "直接映射", m.unit || "—"],
    ["confidence_level", "Claim.confidence_score", "映射为1.0", m.confidence_level || "—"],
  ]));
  k.push(BR());
  
  k.push(P([R("物化SQL示例")],{}));
  let sql = "INSERT INTO claim (claim_id, claim_type, subject, predicate, object_value, confidence_score, case_id, source_system, status, created_at)\n";
  sql += "VALUES ('CLM_METRIC_"+num+"', '"+esc(m.metric_status)+"', '"+esc(m.metric_name_cn||m.metric_name_en)+"', ";
  sql += "'"+esc(m.formula_expression||m.business_definition)+"', '"+esc(m.unit)+"', ";
  sql += "1.0, 'CASE_EAST_PRECISION', 'ORACLE', 'VERIFIED', datetime('now'));\n\n";
  sql += "INSERT INTO evidence (evidence_id, evidence_type, source_system, source_id, claim_id, metric_name, transformation_logic, status, created_at)\n";
  sql += "VALUES ('EVM_METRIC_"+num+"', 'STRUCTURED', 'ORACLE', '"+esc(m.metric_id)+"', 'CLM_METRIC_"+num+"', ";
  sql += "'"+esc(m.metric_name_cn||m.metric_name_en)+"', '"+esc(m.formula_expression)+"', 'VERIFIED', datetime('now'));";
  k.push(...CB(sql));
  k.push(BR());
});

k.push(P([R("4.2 字段血缘 -> Evidence")],{h:HeadingLevel.HEADING_2}));
k.push(P([R("Oracle的field_lineage表记录了每个字段的来源和转换路径，映射为Evidence实体。")],{}));
k.push(BR());

oracle_lineage.slice(0, 10).forEach((l, i) => {
  const num = i + 1;
  k.push(P([R("4.2." + num + " 血缘: " + (l.source_object || "N/A") + " -> " + (l.target_column || "N/A"))],{h:HeadingLevel.HEADING_3}));
  k.push(P([R("源数据（Oracle field_lineage #"+num+"）")],{}));
  k.push(...CB(JSON.stringify(l, null, 2).slice(0, 500)));
  k.push(BR());
  
  k.push(P([R("字段映射表")],{}));
  k.push(TG(["Oracle源字段", "GITS目标字段", "转换规则", "示例值"],[
    ["lineage_id", "Evidence.evidence_id", "前缀转换", l.lineage_id || l.id || "—"],
    ["source_object", "Evidence.source_system", "直接映射", l.source_object || "—"],
    ["source_column", "Evidence.source_id", "直接映射", l.source_column || "—"],
    ["target_column", "Evidence.metric_name", "直接映射", l.target_column || "—"],
    ["transformation_expression", "Evidence.transformation_logic", "直接映射", (l.transformation_expression || "—").toString().slice(0,50)],
  ]));
  k.push(BR());
});

k.push(P([R("4.3 衍生字段 -> Evidence")],{h:HeadingLevel.HEADING_2}));
k.push(P([R("Oracle的derived_field表记录了通过计算得到的衍生字段，映射为Evidence实体。")],{}));
k.push(BR());

oracle_derived.slice(0, 10).forEach((d, i) => {
  const num = i + 1;
  k.push(P([R("4.3." + num + " 衍生字段: " + (d.business_name_cn || d.target_column || "N/A"))],{h:HeadingLevel.HEADING_3}));
  k.push(P([R("源数据（Oracle derived_field #"+num+"）")],{}));
  k.push(...CB(JSON.stringify(d, null, 2).slice(0, 500)));
  k.push(BR());
  
  k.push(P([R("字段映射表")],{}));
  k.push(TG(["Oracle源字段", "GITS目标字段", "转换规则", "示例值"],[
    ["derived_field_id", "Evidence.evidence_id", "前缀转换", d.derived_field_id || d.id || "—"],
    ["target_column", "Evidence.metric_name", "直接映射", d.target_column || d.name || "—"],
    ["transformation_expression", "Evidence.transformation_logic", "直接映射", (d.transformation_expression || "—").toString().slice(0,50)],
    ["source_fields", "Evidence.source_id", "直接映射", (d.source_fields || "—").toString().slice(0,50)],
  ]));
  k.push(BR());
});

k.push(P([R("4.4 分析议题 -> Claim")],{h:HeadingLevel.HEADING_2}));
oracle_issues.forEach((iss, i) => {
  const num = i + 1;
  k.push(P([R("4.4." + num + " 议题: " + (iss.category + ": " + (iss.message||"").slice(0,40)))],{h:HeadingLevel.HEADING_3}));
  k.push(P([R("源数据（Oracle analysis_issue #"+num+"）")],{}));
  k.push(...CB(JSON.stringify(iss, null, 2).slice(0, 500)));
  k.push(BR());
  
  k.push(P([R("字段映射表")],{}));
  k.push(TG(["Oracle源字段", "GITS目标字段", "转换规则", "示例值"],[
    ["issue_id", "Claim.claim_id", "前缀转换", iss.issue_id || iss.id || "—"],
    ["message", "Claim.subject", "直接映射", (iss.message || "—").toString().slice(0,50)],
    ["category", "Claim.claim_type", "枚举映射", iss.category || "—"],
    ["severity", "Claim.predicate", "直接映射", iss.severity || "—"],
    ["status", "Claim.status", "直接映射", iss.status || "—"],
  ]));
  k.push(BR());
});

// === Chapter 5: Database Materialization ===
k.push(P([R("5. 关系型数据库物化方案")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("5.1 完整DDL")],{h:HeadingLevel.HEADING_2}));
k.push(...CB([
  "CREATE TABLE IF NOT EXISTS operating_case (",
  "  case_id TEXT PRIMARY KEY,",
  "  case_name TEXT NOT NULL,",
  "  industry TEXT,",
  "  region TEXT,",
  "  status TEXT DEFAULT 'ACTIVE',",
  "  created_at TEXT DEFAULT datetime('now'),",
  "  updated_at TEXT DEFAULT datetime('now')",
  ");",
  "",
  "CREATE TABLE IF NOT EXISTS claim (",
  "  claim_id TEXT PRIMARY KEY,",
  "  claim_type TEXT NOT NULL,",
  "  subject TEXT,",
  "  predicate TEXT,",
  "  object_value TEXT,",
  "  confidence_score REAL DEFAULT 0.5,",
  "  case_id TEXT REFERENCES operating_case(case_id),",
  "  source_system TEXT CHECK(source_system IN ('ORACLE','HERMES','MANUAL')),",
  "  status TEXT DEFAULT 'PENDING',",
  "  created_at TEXT DEFAULT datetime('now'),",
  "  updated_at TEXT DEFAULT datetime('now')",
  ");",
  "",
  "CREATE TABLE IF NOT EXISTS evidence (",
  "  evidence_id TEXT PRIMARY KEY,",
  "  evidence_type TEXT NOT NULL,",
  "  source_system TEXT,",
  "  source_id TEXT,",
  "  claim_id TEXT REFERENCES claim(claim_id),",
  "  content TEXT,",
  "  metric_name TEXT,",
  "  transformation_logic TEXT,",
  "  status TEXT DEFAULT 'PENDING',",
  "  created_at TEXT DEFAULT datetime('now'),",
  "  updated_at TEXT DEFAULT datetime('now')",
  ");",
  "",
  "CREATE INDEX IF NOT EXISTS idx_claim_case_id ON claim(case_id);",
  "CREATE INDEX IF NOT EXISTS idx_evidence_claim_id ON evidence(claim_id);",
  "CREATE INDEX IF NOT EXISTS idx_claim_source ON claim(source_system);",
].join("\n")));
k.push(BR());

k.push(P([R("5.2 数据量估算")],{h:HeadingLevel.HEADING_2}));
const totalClaims = hermes.length + oracle_metrics.length + oracle_issues.length;
const totalEvidence = oracle_lineage.length + oracle_derived.length;
const totalRecords = 1 + totalClaims + totalEvidence;
k.push(TG(["实体","记录数","来源","存储估算"],[
  ["OperatingCase","1（华东精工）","COV","~1KB"],
  ["Claim",String(totalClaims),"Hermes("+hermes.length+") + Oracle("+oracle_metrics.length+") + Issues("+oracle_issues.length+")","~50KB"],
  ["Evidence",String(totalEvidence),"Lineage("+oracle_lineage.length+") + Derived("+oracle_derived.length+")","~100KB"],
  ["总计",String(totalRecords),"—","~151KB"],
]));
k.push(BR());

// === Chapter 6: Governance ===
k.push(P([R("6. 数据治理规则与校验清单")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("6.1 映射规则")],{h:HeadingLevel.HEADING_2}));
k.push(B("所有Hermes Claim必须映射为Claim实体，不可跳过"));
k.push(B("所有Oracle指标必须映射为Claim+Evidence配对"));
k.push(B("Evidence必须关联到父Claim，不允许孤儿Evidence"));
k.push(B("source_system字段必须为ORACLE/HERMES/MANUAL之一"));
k.push(B("confidence_score范围[0.0, 1.0]"));
k.push(BR());

k.push(P([R("6.2 校验SQL")],{h:HeadingLevel.HEADING_2}));
k.push(...CB([
  "-- 检查孤儿Evidence",
  "SELECT e.evidence_id FROM evidence e",
  "LEFT JOIN claim c ON e.claim_id = c.claim_id",
  "WHERE c.claim_id IS NULL;",
  "",
  "-- 检查缺失source_system",
  "SELECT claim_id FROM claim WHERE source_system IS NULL;",
  "",
  "-- 检查confidence_score范围",
  "SELECT claim_id FROM claim WHERE confidence_score < 0 OR confidence_score > 1;",
  "",
  "-- 统计各源系统Claim数",
  "SELECT source_system, COUNT(*) FROM claim GROUP BY source_system;",
].join("\n")));
k.push(BR());

// === Chapter 7: Appendix ===
k.push(P([R("7. 附录：完整映射索引")],{h:HeadingLevel.HEADING_1}));
k.push(P([R("7.1 Hermes -> Claim 完整索引")],{h:HeadingLevel.HEADING_2}));
const hermesRows = hermes.map((cl, i) => [
  "CLM_"+(i+1),
  cl.claimType || cl.type || "N/A",
  (cl.subject||"").toString().slice(0,40),
  "HERMES",
  cl.id || cl.claimId || "—"
]);
k.push(TG(["GITS Claim ID","Claim类型","Subject摘要","源系统","源ID"], hermesRows));
k.push(BR());

k.push(P([R("7.2 Oracle -> Claim/Evidence 完整索引")],{h:HeadingLevel.HEADING_2}));
const oracleRows = oracle_metrics.map((m, i) => [
  "CLM_METRIC_"+(i+1),
  "METRIC",
  (m.metric_name||m.name||"").toString().slice(0,40),
  "ORACLE",
  m.metric_id || m.id || "—"
]);
k.push(TG(["GITS Claim ID","Claim类型","Subject摘要","源系统","源ID"], oracleRows));
k.push(BR());

k.push(P([R("文档结束")],{h:HeadingLevel.HEADING_1,al:AlignmentType.CENTER}));

// === Generate document ===
async function main() {
  const doc = new Document({
    sections: [{
      properties: {},
      children: k,
    }],
  });

  const buffer = await Packer.toBuffer(doc);
  const outPath = path.join(__dirname, "..", "docs", "数据映射与本体物化报告_华东精工_v1.0.docx");
  fs.writeFileSync(outPath, buffer);
  console.log("Document generated: " + outPath);
  console.log("Total children: " + k.length);
}

main().catch(err => { console.error(err); process.exit(1); });
