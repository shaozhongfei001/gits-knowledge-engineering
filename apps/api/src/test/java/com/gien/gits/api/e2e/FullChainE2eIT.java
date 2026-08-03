package com.gien.gits.api.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.JdbcClaimRepository;
import com.gien.gits.adapter.persistence.JdbcInteractionRepository;
import com.gien.gits.adapter.persistence.JdbcOperatingCaseRepository;
import com.gien.gits.adapter.persistence.scenario.JdbcCustomerJourneyRepository;
import com.gien.gits.api.service.CustomerJourneyService;
import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.customerjourney.InsightClaim;
import com.gien.gits.customerjourney.JourneyPhase;
import com.gien.gits.customerjourney.PostvisitAnalysis;
import com.gien.gits.customerjourney.PrevisitReport;
import com.gien.gits.customerjourney.ProductCandidateClaim;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.OperatingCase;

/**
 * 端到端集成测试：HTTP→H2→完整M17→M22业务链。
 *
 * 模拟"王磊/鑫达贸易"完整剧情：
 *  1. 预置OperatingCase(OPEN) + 初始Claim(CUSTOMER_JOURNEY/CANDIDATE)
 *  2. POST /api/journey/open → 开户+信号交互
 *  3. Service.analyzeInsight → M18 AI洞察
 *  4. Service.matchProduct → M20 产品匹配
 *  5. Service.executePrevisit → M21 访前报告
 *  6. Service.closeWithPostvisit → M22 访后分析
 *  7. 每步验证HTTP/Service返回 + DB数据落库
 *  8. 验证JourneyPhase完整流转: KYC_COLLECT → ... → COMPLETED
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
class FullChainE2eIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private JdbcOperatingCaseRepository caseRepo;
    @Autowired private JdbcClaimRepository claimRepo;
    @Autowired private JdbcInteractionRepository interactionRepo;
    @Autowired private JdbcCustomerJourneyRepository journeyRepo;
    @Autowired private CustomerJourneyService journeyService;

    // ── 预置数据（@BeforeEach确保每测试独立） ──
    private OperatingCase operatingCase;
    private Claim initialClaim;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        // 预置OperatingCase: OPEN状态
        operatingCase = new OperatingCase(
                UUID.randomUUID(), "customer-journey", CaseStatus.OPEN,
                "鑫达贸易跨境结算风险信号", now, null, now, "system-signal");
        caseRepo.save(operatingCase);

        // 预置初始Claim: claimType必须是CUSTOMER_JOURNEY（InsightClaim.fromClaim要求）
        initialClaim = new Claim(
                UUID.randomUUID(), operatingCase.caseId(), "CUSTOMER_JOURNEY",
                ClaimStatus.CANDIDATE, "跨境结算量增长42%，存在汇率风险敞口",
                now, null, now, null);
        claimRepo.save(initialClaim);
    }

    // ══════════════════════════════════════════════════════════════
    //  完整5步链路 E2E 测试
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("M17→M22 完整链路: 王磊/鑫达贸易 业务全流程")
    void fullChain_WangLeiXinDaTrade() throws Exception {
        // ── M17: 开户（通过HTTP POST） ──
        String openBody = """
                {
                    "operatingCaseId": "$caseId",
                    "customerId": "CUST-XINDA-001",
                    "customerName": "鑫达贸易有限公司",
                    "signalDescription": "跨境结算量增长42%%，存在汇率风险敞口"
                }
                """.replace("$caseId", operatingCase.caseId().toString()).replace("%%", "%");

        MvcResult openResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/journey/open")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(openBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journeyId", notNullValue()))
                .andExpect(jsonPath("$.customerId").value("CUST-XINDA-001"))
                .andExpect(jsonPath("$.customerName").value("鑫达贸易有限公司"))
                .andExpect(jsonPath("$.phase").value("KYC_COLLECT"))
                .andReturn();

        JsonNode openJson = objectMapper.readTree(openResult.getResponse().getContentAsString());
        UUID journeyId = UUID.fromString(openJson.get("journeyId").asText());

        // 验证M17 DB数据: CustomerJourney落库 + 信号交互落库 + 案例状态推进
        CustomerJourney savedJourney = journeyRepo.findJourneyById(journeyId).orElseThrow();
        assert savedJourney.phase() == JourneyPhase.KYC_COLLECT : "开户后阶段应为KYC_COLLECT";

        var signalInteractions = interactionRepo.findByCaseId(operatingCase.caseId());
        assert !signalInteractions.isEmpty() : "信号交互应已落库";

        OperatingCase updatedCase = caseRepo.findById(operatingCase.caseId()).orElseThrow();
        assert updatedCase.status() == CaseStatus.IN_PROGRESS : "案例应推进为IN_PROGRESS";

        // ── M18: AI洞察分析（通过Service调用） ──
        InsightClaim insight = journeyService.analyzeInsight(
                initialClaim.claimId(),
                "RISK_EXPOSURE",
                "客户跨境结算量大幅增长，存在汇率风险敞口，建议套期保值",
                journeyId,
                "RM-WANG-LEI", "王磊");

        assert insight != null : "洞察不应为null";
        assert insight.insightCategory().equals("RISK_EXPOSURE") : "洞察类别应为RISK_EXPOSURE";

        // 验证M18 DB: JourneyPhase推进到INSIGHT_ANALYSIS
        CustomerJourney afterInsight = journeyRepo.findJourneyById(journeyId).orElseThrow();
        assert afterInsight.phase() == JourneyPhase.INSIGHT_ANALYSIS : "洞察后阶段应为INSIGHT_ANALYSIS，实际: " + afterInsight.phase();

        // 验证洞察落库
        InsightClaim savedInsight = journeyRepo.findInsightById(insight.insightId()).orElseThrow();
        assert savedInsight.insightSummary().contains("套期保值") : "洞察摘要应包含关键词";

        // ── M20: 产品匹配 ──
        ProductCandidateClaim product = journeyService.matchProduct(
                insight.insightId(),
                "FX-HEDGE-01",
                "远期结售汇",
                "客户跨境结算+套期保值需求匹配远期结售汇产品",
                "RM-WANG-LEI", "王磊");

        assert product != null : "产品候选不应为null";
        assert product.productCode().equals("FX-HEDGE-01") : "产品编码应为FX-HEDGE-01";

        // 验证M20 DB: 产品候选落库
        ProductCandidateClaim savedProduct = journeyRepo.findProductCandidateById(product.productId()).orElseThrow();
        assert savedProduct.productName().equals("远期结售汇") : "产品名称应为远期结售汇";

        // ── M21: 访前报告 ──
        PrevisitReport report = journeyService.executePrevisit(
                operatingCase.caseId(), journeyId,
                "RM-WANG-LEI", "王磊",
                "CUST-XINDA-001", "鑫达贸易-李总",
                "建议拜访客户讨论远期结售汇方案，重点沟通汇率风险管理");

        assert report != null : "访前报告不应为null";
        assert report.summary().contains("远期结售汇") : "报告摘要应包含远期结售汇";

        // 验证M21 DB: JourneyPhase推进到PREVISIT_PREP
        CustomerJourney afterPrevisit = journeyRepo.findJourneyById(journeyId).orElseThrow();
        assert afterPrevisit.phase() == JourneyPhase.PREVISIT_PREP : "访前阶段应为PREVISIT_PREP，实际: " + afterPrevisit.phase();

        // 验证访前报告落库
        PrevisitReport savedReport = journeyRepo.findPrevisitReportById(report.reportId()).orElseThrow();
        assert savedReport.insightIds().contains(insight.insightId()) : "报告应关联洞察ID";

        // ── M22: 访后分析 ──
        PostvisitAnalysis analysis = journeyService.closeWithPostvisit(
                operatingCase.caseId(), journeyId,
                report.reportId(),
                "客户同意试用远期结售汇产品",
                "跟进签约流程，下周一前完成合同签署",
                "RM-WANG-LEI", "王磊",
                "CUST-XINDA-001", "鑫达贸易-李总",
                true);  // 客户同意

        assert analysis != null : "访后分析不应为null";
        assert analysis.outcome().contains("同意") : "访后结果应为客户同意";

        // 验证M22 DB: JourneyPhase推进到COMPLETED
        CustomerJourney afterPostvisit = journeyRepo.findJourneyById(journeyId).orElseThrow();
        assert afterPostvisit.phase() == JourneyPhase.COMPLETED : "访后阶段应为COMPLETED，实际: " + afterPostvisit.phase();

        // 验证访后分析落库
        PostvisitAnalysis savedAnalysis = journeyRepo.findPostvisitAnalysisById(analysis.analysisId()).orElseThrow();
        assert savedAnalysis.followUpAction().contains("签约") : "后续动作应包含签约";

        // ── 最终验证: DB数据完整性 ──
        // 5步共产生5条交互记录
        var allInteractions = interactionRepo.findByCaseId(operatingCase.caseId());
        assert allInteractions.size() >= 5 : "应有至少5条交互记录，实际: " + allInteractions.size();

        // JourneyPhase完整流转验证
        assert afterPostvisit.phase() == JourneyPhase.COMPLETED : "最终阶段应为COMPLETED";
    }

    // ══════════════════════════════════════════════════════════════
    //  HTTP API 独立验证
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("POST /api/journey/open → 开户成功返回CustomerJourney")
    void openJourney_httpApi() throws Exception {
        String body = """
                {
                    "operatingCaseId": "%s",
                    "customerId": "CUST-002",
                    "customerName": "测试客户",
                    "signalDescription": "测试信号描述"
                }
                """.formatted(operatingCase.caseId());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/journey/open")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journeyId", notNullValue()))
                .andExpect(jsonPath("$.operatingCaseId").value(operatingCase.caseId().toString()))
                .andExpect(jsonPath("$.customerId").value("CUST-002"))
                .andExpect(jsonPath("$.phase").value("KYC_COLLECT"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/journey/{id} → 查询旅程详情")
    void getJourney_httpApi() throws Exception {
        // 先开户
        CustomerJourney journey = journeyService.openJourney(
                operatingCase.caseId(), "CUST-003", "查询测试客户", "查询测试信号");

        // 通过HTTP查询
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/journey/{journeyId}", journey.journeyId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journeyId").value(journey.journeyId().toString()))
                .andExpect(jsonPath("$.customerName").value("查询测试客户"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/interaction → 创建交互记录")
    void createInteraction_httpApi() throws Exception {
        // 先开户拿到journeyId
        CustomerJourney journey = journeyService.openJourney(
                operatingCase.caseId(), "CUST-004", "交互测试客户", "交互测试信号");

        String body = """
                {
                    "interactionId": "%s",
                    "caseId": "%s",
                    "journeyId": "%s",
                    "type": "PHONE_CALL",
                    "direction": "OUTBOUND",
                    "channel": "PHONE",
                    "initiator": {
                        "participantId": "RM-001",
                        "role": "RELATIONSHIP_MANAGER",
                        "displayName": "张经理"
                    },
                    "participants": [{
                        "participantId": "CUST-004",
                        "role": "CUSTOMER",
                        "displayName": "交互测试客户"
                    }],
                    "contentSummary": "电话沟通跨境结算需求",
                    "producedClaimIds": [],
                    "outcome": "INFORMATION_GATHERED",
                    "occurredAt": "2026-08-03T10:00:00Z",
                    "endedAt": null,
                    "sourceHash": "e2e-test-hash"
                }
                """.formatted(
                        UUID.randomUUID(), operatingCase.caseId(), journey.journeyId());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/interaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interactionId", notNullValue()))
                .andExpect(jsonPath("$.type").value("PHONE_CALL"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/claim → 创建主张 + 状态变更")
    void claimCrud_httpApi() throws Exception {
        String createBody = """
                {
                    "claimId": "%s",
                    "caseId": "%s",
                    "claimType": "OPPORTUNITY",
                    "status": "CANDIDATE",
                    "statement": "跨境结算增长触发KYC需求",
                    "validFrom": "2026-08-03T10:00:00Z",
                    "validTo": null,
                    "recordedAt": "2026-08-03T10:00:00Z",
                    "supersedesClaimId": null
                }
                """.formatted(UUID.randomUUID(), operatingCase.caseId());

        MvcResult createResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/claim")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimId", notNullValue()))
                .andExpect(jsonPath("$.status").value("CANDIDATE"))
                .andReturn();

        JsonNode claimJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID claimId = UUID.fromString(claimJson.get("claimId").asText());

        // 状态变更
        String statusBody = """
                { "newStatus": "VERIFIED_FACT" }
                """;
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/claim/{claimId}/status", claimId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(statusBody))
                .andExpect(status().isOk());

        // 查询验证
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/claim/{claimId}", claimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED_FACT"));
    }

    @Test
    @Order(6)
    @DisplayName("JDBC直接查询 → DB数据完整性验证")
    void dbIntegrity_jdbcQuery() throws Exception {
        // 执行完整5步链路
        CustomerJourney journey = journeyService.openJourney(
                operatingCase.caseId(), "CUST-DB-001", "DB验证客户", "DB验证信号");

        InsightClaim insight = journeyService.analyzeInsight(
                initialClaim.claimId(), "OPPORTUNITY", "DB验证洞察",
                journey.journeyId(), "RM-DB", "DB客户经理");

        ProductCandidateClaim product = journeyService.matchProduct(
                insight.insightId(), "DB-PROD-01", "DB测试产品",
                "DB匹配原因", "RM-DB", "DB客户经理");

        PrevisitReport report = journeyService.executePrevisit(
                operatingCase.caseId(), journey.journeyId(),
                "RM-DB", "DB客户经理", "CUST-DB-001", "DB客户联系人",
                "DB访前报告摘要");

        journeyService.closeWithPostvisit(
                operatingCase.caseId(), journey.journeyId(),
                report.reportId(), "DB访后结果", "DB后续动作",
                "RM-DB", "DB客户经理", "CUST-DB-001", "DB客户联系人", false);

        // JDBC直接查询验证所有表都有数据
        int journeyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer_journey WHERE journey_id = ?", Integer.class,
                journey.journeyId().toString());
        assert journeyCount == 1 : "customer_journey应有1条记录";

        int insightCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM insight_claim WHERE insight_id = ?", Integer.class,
                insight.insightId().toString());
        assert insightCount == 1 : "insight_claim应有1条记录";

        int productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_candidate_claim WHERE product_id = ?", Integer.class,
                product.productId().toString());
        assert productCount == 1 : "product_candidate_claim应有1条记录";

        int reportCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM previsit_report WHERE report_id = ?", Integer.class,
                report.reportId().toString());
        assert reportCount == 1 : "previsit_report应有1条记录";

        // 验证最终阶段为COMPLETED
        String finalPhase = jdbcTemplate.queryForObject(
                "SELECT phase FROM customer_journey WHERE journey_id = ?", String.class,
                journey.journeyId().toString());
        assert "COMPLETED".equals(finalPhase) : "最终阶段应为COMPLETED，实际: " + finalPhase;

        // 验证交互记录数 >= 5
        int interactionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM interaction WHERE case_id = ?", Integer.class,
                operatingCase.caseId().toString());
        assert interactionCount >= 5 : "交互记录应>=5条，实际: " + interactionCount;
    }
}