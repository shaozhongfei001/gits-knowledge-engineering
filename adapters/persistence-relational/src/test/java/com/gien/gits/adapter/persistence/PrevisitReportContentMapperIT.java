package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PrevisitReportContentMapper;
import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.PrevisitReportContent.CustomerOverview;
import com.gien.gits.engagement.PrevisitReportContent.KycGapSummary;
import com.gien.gits.engagement.PrevisitReportContent.ProductScheme;

/**
 * Integration test for PrevisitReportContentMapper — verifies productSchemes
 * uses ProductSchemeListTypeHandler for correct serialization/deserialization,
 * and JSON fields (customerOverview, kycGapSummary) round-trip correctly.
 */
class PrevisitReportContentMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-PR-CASE-001";
    private static final String JOURNEY_ID = "IT-PR-JNY-001";

    @Test
    void insertAndFindByReportId() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        CustomerOverview overview = new CustomerOverview(
                "制造业", "LARGE", "VIP", 50_000_000L, "LOW", "长期合作客户");
        KycGapSummary kycGap = new KycGapSummary(
                List.of("营业执照", "财务报表"), List.of("法人信息"), List.of("实际控制人"), List.of("请补充实际控制人信息"));
        List<ProductScheme> schemes = List.of(
                new ProductScheme("P001", "流动资金贷款", "客户有短期融资需求", "500万", "1年",
                        List.of("年利率4.5%"), List.of("财务报表", "纳税证明"), List.of("需关注行业风险")),
                new ProductScheme("P002", "供应链融资", "客户有供应链结算需求", "1000万", "2年",
                        List.of("年利率4.0%"), List.of("贸易合同", "发票"), List.of("需核实贸易背景"))
        );

        PrevisitReportContent content = new PrevisitReportContent(
                "IT-PR-001",            // reportId
                "IT-CUST-001",          // customerId
                "测试客户有限公司",       // customerName
                "张经理",                // rmName
                "了解客户融资需求",       // visitObjective
                overview,               // customerOverview
                kycGap,                 // kycGapSummary
                schemes,                // productSchemes
                List.of("客户是否有跨境业务？", "是否有供应链融资需求？"),  // keyQuestions
                List.of("关注行业周期性风险", "核实贸易背景真实性"),       // riskReminders
                "重点推荐流动资金贷款方案" // visitStrategy
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            PrevisitReportContentMapper mapper = session.getMapper(PrevisitReportContentMapper.class);
            mapper.insert(content, JOURNEY_ID, CASE_ID);

            Optional<PrevisitReportContent> found = mapper.findByReportId("IT-PR-001");

            assertThat(found).isPresent();
            PrevisitReportContent actual = found.get();
            assertThat(actual.reportId()).isEqualTo("IT-PR-001");
            assertThat(actual.customerId()).isEqualTo("IT-CUST-001");
            assertThat(actual.customerName()).isEqualTo("测试客户有限公司");
            assertThat(actual.rmName()).isEqualTo("张经理");
            assertThat(actual.visitObjective()).isEqualTo("了解客户融资需求");
            assertThat(actual.visitStrategy()).isEqualTo("重点推荐流动资金贷款方案");
        }
    }

    @Test
    void productSchemesSerializationRoundTrip() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        List<ProductScheme> schemes = List.of(
                new ProductScheme("P001", "流动资金贷款", "匹配度高", "500万", "1年",
                        List.of("年利率4.5%"), List.of("财务报表"), List.of("行业风险")),
                new ProductScheme("P002", "信用证", "有贸易背景", "200万", "6个月",
                        List.of("手续费0.1%"), List.of("贸易合同"), List.of("需核实真实性"))
        );

        PrevisitReportContent content = new PrevisitReportContent(
                "IT-PR-002", "IT-CUST-001", "客户公司", "李经理", "讨论方案",
                new CustomerOverview("IT", "MEDIUM", "NORMAL", 10_000_000L, "MEDIUM", "新客户"),
                new KycGapSummary(List.of(), List.of(), List.of("全部信息"), List.of()),
                schemes, List.of("问题1"), List.of("提醒1"), "推荐信用证"
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            PrevisitReportContentMapper mapper = session.getMapper(PrevisitReportContentMapper.class);
            mapper.insert(content, JOURNEY_ID, CASE_ID);

            Optional<PrevisitReportContent> found = mapper.findByReportId("IT-PR-002");

            assertThat(found).isPresent();
            List<ProductScheme> actualSchemes = found.get().productSchemes();
            assertThat(actualSchemes).hasSize(2);

            ProductScheme first = actualSchemes.get(0);
            assertThat(first.productId()).isEqualTo("P001");
            assertThat(first.productName()).isEqualTo("流动资金贷款");
            assertThat(first.matchReason()).isEqualTo("匹配度高");
            assertThat(first.suggestedAmount()).isEqualTo("500万");
            assertThat(first.suggestedTerm()).isEqualTo("1年");
            assertThat(first.keyConditions()).containsExactly("年利率4.5%");
            assertThat(first.requiredMaterials()).containsExactly("财务报表");
            assertThat(first.riskPoints()).containsExactly("行业风险");

            ProductScheme second = actualSchemes.get(1);
            assertThat(second.productId()).isEqualTo("P002");
            assertThat(second.productName()).isEqualTo("信用证");
            assertThat(second.keyConditions()).containsExactly("手续费0.1%");
        }
    }

    @Test
    void jsonFieldsSerialization() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        CustomerOverview overview = new CustomerOverview(
                "金融业", "LARGE", "VVIP", 100_000_000L, "LOW", "核心客户");
        KycGapSummary kycGap = new KycGapSummary(
                List.of("营业执照"), List.of("财务报表"), List.of("实际控制人"), List.of("补充控制人信息"));

        PrevisitReportContent content = new PrevisitReportContent(
                "IT-PR-003", "IT-CUST-001", "金融客户", "王经理", "KYC更新",
                overview, kycGap, List.of(), List.of("问题1", "问题2"), List.of("提醒1"), "策略"
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            PrevisitReportContentMapper mapper = session.getMapper(PrevisitReportContentMapper.class);
            mapper.insert(content, JOURNEY_ID, CASE_ID);

            Optional<PrevisitReportContent> found = mapper.findByReportId("IT-PR-003");

            assertThat(found).isPresent();
            CustomerOverview actualOverview = found.get().customerOverview();
            assertThat(actualOverview.industry()).isEqualTo("金融业");
            assertThat(actualOverview.enterpriseScale()).isEqualTo("LARGE");
            assertThat(actualOverview.customerTier()).isEqualTo("VVIP");
            assertThat(actualOverview.registeredCapitalCny()).isEqualTo(100_000_000L);
            assertThat(actualOverview.riskLevel()).isEqualTo("LOW");
            assertThat(actualOverview.relationshipSummary()).isEqualTo("核心客户");

            KycGapSummary actualKyc = found.get().kycGapSummary();
            assertThat(actualKyc.knownItems()).containsExactly("营业执照");
            assertThat(actualKyc.partialKnownItems()).containsExactly("财务报表");
            assertThat(actualKyc.unknownItems()).containsExactly("实际控制人");
            assertThat(actualKyc.priorityQuestions()).containsExactly("补充控制人信息");

            assertThat(found.get().keyQuestions()).containsExactly("问题1", "问题2");
            assertThat(found.get().riskReminders()).containsExactly("提醒1");
        }
    }
}
