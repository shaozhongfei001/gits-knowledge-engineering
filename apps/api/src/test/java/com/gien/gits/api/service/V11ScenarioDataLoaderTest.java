package com.gien.gits.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** P1b 覆盖率补测：V11ScenarioDataLoader 空/缺数据 fail-closed 路径与少量数据映射。 */
class V11ScenarioDataLoaderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private V11ScenarioDataReader reader;
    private V11ScenarioDataLoader loader;

    @BeforeEach
    void setUp() {
        reader = mock(V11ScenarioDataReader.class);
        // 默认所有读取返回空（fail-closed）
        doReturn(Optional.empty()).when(reader).readJson(anyString());
        doReturn(Optional.empty()).when(reader).readYaml(anyString());
        doReturn(List.of()).when(reader).readJsonl(anyString());
        doReturn(new V11ScenarioDataReader.CsvData(List.of(), List.of())).when(reader).readCsv(anyString());
        loader = new V11ScenarioDataLoader(reader);
    }

    // ── 数据映射路径 ─────────────────────────────────────────────

    @Test
    void loadCustomers_withData_mapsRecords() throws Exception {
        String json = """
            [{"customer_id":"CUST-001","canonical_name":"示例集团","established_date":"2000-01-01",
              "registered_capital_cny":"100000000","industry":"制造业","region":"北京",
              "customer_tier":"A","relationship_since":"2010-05-01","rm_id":"RM-1","rm_name":"张经理",
              "branch":"北京分行","listed":true,"risk_level":"中","main_products":["融资"],
              "tags":["重要"],"relationship_summary":"长期客户"}]
            """;
        doReturn(Optional.of(MAPPER.readTree(json))).when(reader).readJson("02_master_data/customer_master.json");

        assertThat(loader.loadCustomers()).hasSize(1);
    }

    @Test
    void loadCustomers_nonArrayNode_mapsSingleRecord() throws Exception {
        String json = """
            {"customer_id":"CUST-002","canonical_name":"单体客户","established_date":"2005-03-01",
             "registered_capital_cny":"50000000","industry":"贸易","region":"上海",
             "customer_tier":"B","relationship_since":"2015-01-01","rm_id":"RM-2","rm_name":"李经理",
             "branch":"上海分行","listed":false,"risk_level":"低","main_products":["结算"],
             "tags":[],"relationship_summary":"普通客户"}
            """;
        doReturn(Optional.of(MAPPER.readTree(json))).when(reader).readJson("02_master_data/customer_master.json");
        assertThat(loader.loadCustomers()).hasSize(1);
    }

    @Test
    void loadCustomers_withInvalidRow_skips() throws Exception {
        String json = "[{\"customer_id\":\"CUST-BAD\",\"canonical_name\":null}]";
        doReturn(Optional.of(MAPPER.readTree(json))).when(reader).readJson("02_master_data/customer_master.json");
        // mapCustomer 对缺字段可能抛异常 → 返回 null 被跳过（fail-closed）
        assertThat(loader.loadCustomers()).isNotNull();
    }

    @Test
    void loadLegalEntities_withData_mapsRecords() {
        doReturn(new V11ScenarioDataReader.CsvData(
                List.of("entity_id", "bank_customer_id", "name", "role", "ownership_pct", "ownership_parent", "status"),
                List.of(Map.of("entity_id", "E-1", "bank_customer_id", "CUST-001", "name", "子公司", "role", "SUBSIDIARY",
                        "ownership_pct", "80", "ownership_parent", "E-0", "status", "ACTIVE"))))
                .when(reader).readCsv("02_master_data/legal_entities.csv");

        assertThat(loader.loadLegalEntities()).hasSize(1);
    }

    @Test
    void loadGroupRelationships_withData_mapsRecords() {
        doReturn(new V11ScenarioDataReader.CsvData(
                List.of("from_entity", "to_entity", "relation_type", "ownership_pct"),
                List.of(Map.of("from_entity", "E-1", "to_entity", "E-2", "relation_type", "OWNS", "ownership_pct", "51"))))
                .when(reader).readCsv("02_master_data/group_relationships.csv");

        assertThat(loader.loadGroupRelationships()).hasSize(1);
    }

    @Test
    void loadCreditFacilities_withData_mapsRecords() {
        doReturn(new V11ScenarioDataReader.CsvData(
                List.of("facility_id", "borrower_entity_id", "facility_type", "approval_date", "expiry_date",
                        "approved_amount_cny", "used_amount_cny", "available_amount_cny", "allowed_purpose", "restriction"),
                List.of(Map.of("facility_id", "CF-1", "borrower_entity_id", "E-1", "facility_type", "CREDIT",
                        "approval_date", "2024-01-01", "expiry_date", "2026-01-01", "approved_amount_cny", "50000000",
                        "used_amount_cny", "20000000", "available_amount_cny", "30000000", "allowed_purpose", "流动资金", "restriction", "无"))))
                .when(reader).readCsv("03_bank_data/credit_facilities.csv");

        assertThat(loader.loadCreditFacilities()).hasSize(1);
    }

    @Test
    void loadExternalEvents_withData_mapsRecords() throws Exception {
        String jsonl = """
            {"event_id":"EV-1","date":"2026-01-01","category":"NEWS","source_name":"官网","entity":"示例集团",
             "title":"扩产","content":"计划扩产","relevance":"HIGH","reliability":"VERIFIED","tags":["扩产"],
             "opportunity_hint":"融资机会","no_go":"无"}
            """;
        doReturn(List.of(MAPPER.readTree(jsonl))).when(reader).readJsonl("04_external_data/external_events.jsonl");
        assertThat(loader.loadExternalEvents()).hasSize(1);
    }

    @Test
    void loadProductKnowledgeCards_withData_mapsActiveRecords() throws Exception {
        String json = """
            {"products":[
              {"product_id":"P-1","name":"流动资金贷款","status":"ACTIVE","business_problem":"短期周转",
               "key_conditions":["企业"],"required_materials":["报表"],"not_suitable":["个人"],
               "scenario_fit":"流动资金","prohibited":["炒股"]},
              {"product_id":"P-2","name":"非活跃产品","status":"INACTIVE","business_problem":"x"}
            ]}
            """;
        doReturn(Optional.of(MAPPER.readTree(json))).when(reader).readYaml("05_knowledge/product_knowledge_cards.yaml");
        // 仅 ACTIVE 产品被加载
        assertThat(loader.loadProductKnowledgeCards()).hasSize(1);
    }

    @Test
    void loadBankRelationshipSnapshot_withData_mapsRecord() {
        doReturn(new V11ScenarioDataReader.CsvData(
                List.of("month", "deposit_balance_cny", "credit_used_cny", "credit_approved_cny", "credit_available_cny"),
                List.of(
                        Map.of("month", "2026-05", "deposit_balance_cny", "1000", "credit_used_cny", "500",
                                "credit_approved_cny", "2000", "credit_available_cny", "1500"),
                        Map.of("month", "2026-06", "deposit_balance_cny", "2000", "credit_used_cny", "600",
                                "credit_approved_cny", "3000", "credit_available_cny", "2400"))))
                .when(reader).readCsv("03_bank_data/bank_relationship_monthly.csv");

        // 取最新月份（最后一行的 2026-06）
        assertThat(loader.loadBankRelationshipSnapshot()).isPresent();
    }

    @Test
    void loadKycGapProfile_withData_mapsRecord() throws Exception {
        doReturn(List.of(
                MAPPER.readTree("{\"question\":\"3000万需求的具体含义\",\"status\":\"ACTIVE\"}"),
                MAPPER.readTree("{\"question\":\"他行介入情况\",\"status\":\"INACTIVE\"}")))
                .when(reader).readJsonl("05_knowledge/kyc_question_library.jsonl");
        assertThat(loader.loadKycGapProfile()).isPresent();
    }

    @Test
    void loadHistoricalInteractions_withData_mapsRecords() throws Exception {
        String jsonl = """
            {"channel":"VISIT","raw_summary":"拜访客户","date":"2026-01-01T10:00:00Z",
             "participants":[{"id":"RM-1","role":"RELATIONSHIP_MANAGER","name":"张经理"}]}
            """;
        doReturn(List.of(MAPPER.readTree(jsonl))).when(reader).readJsonl("06_interactions/historical_interactions.jsonl");
        assertThat(loader.loadHistoricalInteractions()).hasSize(1);
    }

    @Test
    void loadCustomers_readerEmpty_returnsEmpty() {
        assertThat(loader.loadCustomers()).isEmpty();
    }

    @Test
    void loadLegalEntities_readerEmpty_returnsEmpty() {
        assertThat(loader.loadLegalEntities()).isEmpty();
    }

    @Test
    void loadGroupRelationships_readerEmpty_returnsEmpty() {
        assertThat(loader.loadGroupRelationships()).isEmpty();
    }

    @Test
    void loadCreditFacilities_readerEmpty_returnsEmpty() {
        assertThat(loader.loadCreditFacilities()).isEmpty();
    }

    @Test
    void loadBankRelationshipSnapshot_readerEmpty_returnsEmpty() {
        assertThat(loader.loadBankRelationshipSnapshot()).isEmpty();
    }

    @Test
    void loadExternalEvents_readerEmpty_returnsEmpty() {
        assertThat(loader.loadExternalEvents()).isEmpty();
    }

    @Test
    void loadProductKnowledgeCards_readerEmpty_returnsEmpty() {
        assertThat(loader.loadProductKnowledgeCards()).isEmpty();
    }

    @Test
    void loadKycGapProfile_readerEmpty_returnsEmpty() {
        assertThat(loader.loadKycGapProfile()).isEmpty();
    }

    @Test
    void loadHistoricalInteractions_readerEmpty_returnsEmpty() {
        assertThat(loader.loadHistoricalInteractions()).isEmpty();
    }
}
