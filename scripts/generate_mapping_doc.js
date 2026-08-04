const fs = require('fs');
const path = require('path');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, HeadingLevel, AlignmentType, BorderStyle, UnderlineType, TabStopPosition, TabStopType } = require('docx');

// 读取源数据文件
const hermesData = JSON.parse(fs.readFileSync(path.join(__dirname, '../specs/data/hermes-seed-data.v0.1.json'), 'utf-8'));
const oracleData = JSON.parse(fs.readFileSync(path.join(__dirname, '../specs/data/oracle-seed-claims.v0.1.json'), 'utf-8'));
const linkmlSchema = JSON.parse(fs.readFileSync(path.join(__dirname, '../specs/semantic/gits-core.linkml.yaml'), 'utf-8'));

// Oracle元数据目录
const oracleCatalog = {
  metrics: [
    { id: "METRIC_001", name: "客户余额", code: "CUST_BALANCE", table: "DWH_CUST_BALANCE", formula: "SUM(amount)", unit: "元" },
    { id: "METRIC_002", name: "授信使用率", code: "CREDIT_USAGE_RATE", table: "DWH_CREDIT_USAGE", formula: "used_credit / total_credit", unit: "%" },
    { id: "METRIC_003", name: "贷款逾期天数", code: "LOAN_OVERDUE_DAYS", table: "DWH_LOAN_OVERDUE", formula: "DATEDIFF(current_date, due_date)", unit: "天" }
  ],
  lineage: [
    { source: "EDWCRM.A_ZHCX_CUST_BASE", target: "DWH_CUST_BALANCE", field: "CUSTID -> customer_id" },
    { source: "EDWCRM.A_ZHCX_CREDIT_INFO", target: "DWH_CREDIT_USAGE", field: "CUSTID -> customer_id" },
    { source: "EDWCRM.A_ZHCX_LOAN_INFO", target: "DWH_LOAN_OVERDUE", field: "CUSTID -> customer_id" }
  ]
};

const doc = new Document({
  sections: [{
    properties: {},
    children: [
      // 封面
      new Paragraph({
        text: '数据映射与本体物化报告',
        heading: HeadingLevel.TITLE,
        alignment: AlignmentType.CENTER,
        spacing: { after: 200 }
      }),
      new Paragraph({
        text: '华东精工客户经营用例',
        heading: HeadingLevel.HEADING_1,
        alignment: AlignmentType.CENTER,
        spacing: { after: 200 }
      }),
      new Paragraph({
        text: '展示Oracle结构化数据与Hermes非结构化数据如何映射到GITS本体模型',
        heading: HeadingLevel.HEADING_2,
        alignment: AlignmentType.CENTER,
        spacing: { after: 400 }
      }),

      // 目录
      new Paragraph({
        text: '目录',
        heading: HeadingLevel.HEADING_1,
        spacing: { after: 200 }
      }),
      new Paragraph({ text: '1. 概述', heading: HeadingLevel.HEADING_2 }),
      new Paragraph({ text: '2. 数据源概览', heading: HeadingLevel.HEADING_2 }),
      new Paragraph({ text: '3. 本体模型定义', heading: HeadingLevel.HEADING_2 }),
      new Paragraph({ text: '4. 数据映射过程', heading: HeadingLevel.HEADING_2 }),
      new Paragraph({ text: '5. 物化存储结构', heading: HeadingLevel.HEADING_2 }),
      new Paragraph({ text: '6. 验证与追溯', heading: HeadingLevel.HEADING_2 }),

      // 1. 概述
      new Paragraph({
        text: '1. 概述',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: '本报告以"华东精工客户经营"为用例，详细展示：',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '• Oracle结构化数据：指标定义、血缘关系、数据质量规则',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '• Hermes非结构化数据：客户经营视图、场景剧本、事实对账报告',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '这些数据如何映射到GITS本体模型（OperatingCase, Claim, Evidence）并物化为关系型数据库记录。',
        spacing: { after: 200 }
      }),

      // 2. 数据源概览
      new Paragraph({
        text: '2. 数据源概览',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),

      // 2.1 Oracle数据
      new Paragraph({
        text: '2.1 Oracle结构化数据',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '来源：Oracle元数据编目库 (oracle_metadata_catalog.sqlite)',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '包含：',
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '指标定义表 (metric_definition): ', bold: true }),
          new TextRun('定义了客户余额、授信使用率等3个核心指标的计算公式和业务含义')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '字段血缘表 (field_lineage): ', bold: true }),
          new TextRun('记录了从源系统(EDWCRM)到目标数仓表的数据血缘关系')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '衍生字段表 (derived_field): ', bold: true }),
          new TextRun('定义了客户状态、风险等级等衍生字段的计算逻辑')
        ],
        spacing: { after: 100 }
      }),

      // 2.2 Hermes数据
      new Paragraph({
        text: '2.2 Hermes非结构化数据',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '来源：Hermes演示数据包 (Hermes演示数据包_V1.0_RUN_001/)',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '包含：',
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '客户经营视图 (04_CUSTOMER_OPERATING_VIEW_RENDERED.md): ', bold: true }),
          new TextRun('华东精工(CUST-001)的业务数据，包括余额、授信、贷款等指标')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '场景剧本 (scenarios/): ', bold: true }),
          new TextRun('10个业务场景剧本，描述客户经营的各种业务场景')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '事实对账报告 (07_FACT_RECONCILIATION_REPORT.md): ', bold: true }),
          new TextRun('展示业务事实与AI生成声明的对账结果')
        ],
        spacing: { after: 100 }
      }),

      // 3. 本体模型定义
      new Paragraph({
        text: '3. 本体模型定义',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: 'GITS本体模型定义了以下核心实体：',
        spacing: { after: 200 }
      }),

      // OperatingCase
      new Paragraph({
        text: '3.1 OperatingCase（经营案例）',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '描述：一个持续的业务事项，跨越多个交互事件',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: '属性', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '类型', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '说明', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('case_id')] }),
              new TableCell({ children: [new Paragraph('UUID')] }),
              new TableCell({ children: [new Paragraph('主键')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('case_type')] }),
              new TableCell({ children: [new Paragraph('Enum')] }),
              new TableCell({ children: [new Paragraph('案例类型')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('status')] }),
              new TableCell({ children: [new Paragraph('Enum')] }),
              new TableCell({ children: [new Paragraph('案例状态')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('purpose')] }),
              new TableCell({ children: [new Paragraph('String')] }),
              new TableCell({ children: [new Paragraph('业务目的')] })
            ]
          })
        ]
      }),

      // Claim
      new Paragraph({
        text: '3.2 Claim（声明）',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '描述：一个类型的断言，与权威事实分离',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: '属性', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '类型', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '说明', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('claim_id')] }),
              new TableCell({ children: [new Paragraph('UUID')] }),
              new TableCell({ children: [new Paragraph('主键')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('case_id')] }),
              new TableCell({ children: [new Paragraph('UUID')] }),
              new TableCell({ children: [new Paragraph('关联的经营案例')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('claim_type')] }),
              new TableCell({ children: [new Paragraph('Enum')] }),
              new TableCell({ children: [new Paragraph('声明类型')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('statement')] }),
              new TableCell({ children: [new Paragraph('String')] }),
              new TableCell({ children: [new Paragraph('声明内容')] })
            ]
          })
        ]
      }),

      // Evidence
      new Paragraph({
        text: '3.3 Evidence（证据）',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '描述：支持、反驳或限定一个声明的可定位源',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: '属性', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '类型', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '说明', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('evidence_id')] }),
              new TableCell({ children: [new Paragraph('UUID')] }),
              new TableCell({ children: [new Paragraph('主键')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('source_uri')] }),
              new TableCell({ children: [new Paragraph('URI')] }),
              new TableCell({ children: [new Paragraph('数据源URI')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('source_version')] }),
              new TableCell({ children: [new Paragraph('String')] }),
              new TableCell({ children: [new Paragraph('数据源版本')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('locator')] }),
              new TableCell({ children: [new Paragraph('String')] }),
              new TableCell({ children: [new Paragraph('定位器')] })
            ]
          })
        ]
      }),

      // 4. 数据映射过程
      new Paragraph({
        text: '4. 数据映射过程',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),

      // 4.1 Hermes到OperatingCase
      new Paragraph({
        text: '4.1 Hermes非结构化数据 → OperatingCase',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '映射规则：',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '从客户经营视图文档中提取业务场景，创建对应的OperatingCase：',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: 'Hermes源数据', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '本体模型', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('客户经营视图文档')] }),
              new TableCell({ children: [new Paragraph('OperatingCase')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('华东精工 (CUST-001)')] }),
              new TableCell({ children: [new Paragraph('case_id = UUID')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('客户经营场景')] }),
              new TableCell({ children: [new Paragraph('case_type = CUSTOMER_OPERATING')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('进行中')] }),
              new TableCell({ children: [new Paragraph('status = ACTIVE')] })
            ]
          })
        ]
      }),

      // 4.2 Oracle到Claim
      new Paragraph({
        text: '4.2 Oracle结构化数据 → Claim',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '映射规则：',
        spacing: { after: 100 }
      }),
      new Paragraph({
        text: '从Oracle指标定义和血缘关系中提取数据质量声明：',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: 'Oracle源数据', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: '本体模型', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('指标定义表')] }),
              new TableCell({ children: [new Paragraph('Claim (METRIC_DEFINITION)')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('字段血缘表')] }),
              new TableCell({ children: [new Paragraph('Claim (FIELD_LINEAGE)')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('衍生字段表')] }),
              new TableCell({ children: [new Paragraph('Claim (DERIVED_FIELD)')] })
            ]
          })
        ]
      }),

      // 4.3 证据关联
      new Paragraph({
        text: '4.3 证据关联',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: '为每个Claim创建对应的Evidence，指向原始数据源：',
        spacing: { after: 100 }
      }),
      new Table({
        width: { size: 100, type: 'percent' },
        rows: [
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph({ text: 'Claim类型', bold: true })] }),
              new TableCell({ children: [new Paragraph({ text: 'Evidence来源', bold: true })] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('METRIC_DEFINITION')] }),
              new TableCell({ children: [new Paragraph('oracle_metadata_catalog.sqlite')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('FIELD_LINEAGE')] }),
              new TableCell({ children: [new Paragraph('oracle_metadata_catalog.sqlite')] })
            ]
          }),
          new TableRow({
            children: [
              new TableCell({ children: [new Paragraph('CUSTOMER_BALANCE')] }),
              new TableCell({ children: [new Paragraph('04_CUSTOMER_OPERATING_VIEW_RENDERED.md')] })
            ]
          })
        ]
      }),

      // 5. 物化存储结构
      new Paragraph({
        text: '5. 物化存储结构',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: '本体模型物化为以下关系型数据库表：',
        spacing: { after: 200 }
      }),

      // 5.1 operating_case表
      new Paragraph({
        text: '5.1 operating_case表',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: 'CREATE TABLE operating_case (',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  case_id UUID PRIMARY KEY,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  case_type VARCHAR(50) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  status VARCHAR(20) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  purpose TEXT NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  valid_from TIMESTAMP NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  valid_to TIMESTAMP,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  recorded_at TIMESTAMP NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  created_by VARCHAR(100) NOT NULL',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: ');',
        spacing: { after: 200 }
      }),

      // 5.2 claim表
      new Paragraph({
        text: '5.2 claim表',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: 'CREATE TABLE claim (',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  claim_id UUID PRIMARY KEY,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  case_id UUID REFERENCES operating_case(case_id),',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  claim_type VARCHAR(50) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  claim_status VARCHAR(20) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  statement TEXT NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  valid_from TIMESTAMP,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  valid_to TIMESTAMP,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  recorded_at TIMESTAMP NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  supersedes_claim_id UUID',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: ');',
        spacing: { after: 200 }
      }),

      // 5.3 evidence表
      new Paragraph({
        text: '5.3 evidence表',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: 'CREATE TABLE evidence (',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  evidence_id UUID PRIMARY KEY,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  source_uri VARCHAR(500) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  source_version VARCHAR(50) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  locator VARCHAR(500) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  content_hash VARCHAR(64) NOT NULL,',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  permission_label VARCHAR(50) NOT NULL',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: ');',
        spacing: { after: 200 }
      }),

      // 5.4 claim_evidence关联表
      new Paragraph({
        text: '5.4 claim_evidence关联表',
        heading: HeadingLevel.HEADING_2,
        spacing: { before: 200, after: 100 }
      }),
      new Paragraph({
        text: 'CREATE TABLE claim_evidence (',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  claim_id UUID REFERENCES claim(claim_id),',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  evidence_id UUID REFERENCES evidence(evidence_id),',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  relationship VARCHAR(20) NOT NULL, -- SUPPORTS, REFUTES, QUALIFIES',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: '  PRIMARY KEY (claim_id, evidence_id)',
        spacing: { after: 50 }
      }),
      new Paragraph({
        text: ');',
        spacing: { after: 200 }
      }),

      // 6. 验证与追溯
      new Paragraph({
        text: '6. 验证与追溯',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: '通过本体模型的物化，实现了以下能力：',
        spacing: { after: 200 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '数据追溯：', bold: true }),
          new TextRun('从业务声明追溯到原始数据源（Oracle指标定义、Hermes业务文档）')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '证据链：', bold: true }),
          new TextRun('每个声明都有对应的证据支持，形成完整的证据链')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '版本控制：', bold: true }),
          new TextRun('通过valid_from/valid_to实现时间维度上的版本控制')
        ],
        spacing: { after: 100 }
      }),
      new Paragraph({
        children: [
          new TextRun({ text: '状态管理：', bold: true }),
          new TextRun('通过claim_status管理声明的生命周期（PROPOSED, VERIFIED, REJECTED）')
        ],
        spacing: { after: 100 }
      }),

      // 总结
      new Paragraph({
        text: '总结',
        heading: HeadingLevel.HEADING_1,
        spacing: { before: 400, after: 200 }
      }),
      new Paragraph({
        text: '通过GITS本体模型，成功将Oracle结构化数据（指标定义、血缘关系）和Hermes非结构化数据（客户经营视图、场景剧本）映射为统一的本体实例，并物化为关系型数据库记录。这种设计实现了数据源的统一管理和追溯，为业务演示提供了真实场景支持。',
        spacing: { after: 200 }
      })
    ]
  }]
});

// 生成文档
Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync(path.join(__dirname, '../docs/数据映射与本体物化报告_华东精工.docx'), buffer);
  console.log('Word文档已生成：docs/数据映射与本体物化报告_华东精工.docx');
});
