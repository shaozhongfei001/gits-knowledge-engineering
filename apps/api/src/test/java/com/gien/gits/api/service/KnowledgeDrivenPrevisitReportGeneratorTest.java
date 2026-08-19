package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.port.KnowledgeElementPort;
import com.gien.gits.knowledge.port.KnowledgeWikiPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 知识地图驱动访前报告生成单元测试：LLM 读图增强 + fail-closed fallback。
 */
class KnowledgeDrivenPrevisitReportGeneratorTest {

    private LlmClient llmClient;
    private PrevisitWorkflowService workflowService;
    private KnowledgeDrivenPrevisitReportGenerator generator;

    @BeforeEach
    void setUp() {
        KnowledgeElementPort elementPort = mock(KnowledgeElementPort.class);
        KnowledgeWikiPort wikiPort = mock(KnowledgeWikiPort.class);
        when(wikiPort.renderMap(anyString())).thenReturn("# Knowledge Map [AUTHORITATIVE]");
        when(elementPort.listByKnowledgeItem("KI-009"))
                .thenReturn(List.of(new KnowledgeElement("1.0.0", "KE-009-01", "客户全称", "K-Type-F",
                        "KI-009", "企业工商注册的完整法定名称",
                        new KnowledgeElement.Source("CRM系统", "AUTHORITATIVE"), List.of(), "ACTIVE")));
        KnowledgeAssembler assembler = new KnowledgeAssembler(elementPort, wikiPort);

        ActivationContractPort contractPort = mock(ActivationContractPort.class);
        when(contractPort.find("AC-PREVISIT-001"))
                .thenReturn(Optional.of(contract("AC-PREVISIT-001", List.of("KI-009"))));

        llmClient = mock(LlmClient.class);
        workflowService = mock(PrevisitWorkflowService.class);
        when(workflowService.generatePrevisitReport(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(baseReport());

        generator = new KnowledgeDrivenPrevisitReportGenerator(
                assembler, llmClient, workflowService, contractPort);
    }

    @Test
    void llmJsonEnhancesStrategyAndQuestions() {
        when(llmClient.complete(anyString(), anyString())).thenReturn(
                "{\"visitStrategy\":\"以设备采购融资为核心，重点补全授信需求细节。\","
                        + "\"keyQuestions\":[\"Q1 确认融资额度\",\"Q2 了解产能扩张计划\"],"
                        + "\"riskReminders\":[\"关注出口订单波动\"]}");

        PrevisitReportContent result = generator.generate("CUST-001", "journey", "OC-001", "设备融资");

        assertEquals("以设备采购融资为核心，重点补全授信需求细节。", result.visitStrategy());
        assertTrue(result.keyQuestions().contains("Q1 确认融资额度"));
        assertTrue(result.riskReminders().contains("关注出口订单波动"));
        verify(llmClient).complete(anyString(), anyString());
    }

    @Test
    void llmFailureFallsBackToRuleBase() {
        when(llmClient.complete(anyString(), anyString())).thenThrow(new LlmClientException("llm down"));

        PrevisitReportContent result = generator.generate("CUST-001", "journey", "OC-001", "设备融资");

        // fallback 到规则版：保留 base 的策略/问题
        assertEquals("规则策略：先了解现状再补KYC缺口", result.visitStrategy());
        assertEquals(List.of("基础问题1"), result.keyQuestions());
    }

    @Test
    void contractMissingYieldsEmptyKiButStillGenerates() {
        ActivationContractPort emptyContract = mock(ActivationContractPort.class);
        when(emptyContract.find("AC-PREVISIT-001")).thenReturn(Optional.empty());
        KnowledgeWikiPort emptyWiki = mock(KnowledgeWikiPort.class);
        when(emptyWiki.renderMap(anyString())).thenReturn("");
        generator = new KnowledgeDrivenPrevisitReportGenerator(
                new KnowledgeAssembler(mock(KnowledgeElementPort.class), emptyWiki),
                llmClient, workflowService, emptyContract);
        when(llmClient.complete(anyString(), anyString())).thenReturn("{}");

        PrevisitReportContent result = generator.generate("CUST-001", "journey", "OC-001", "设备融资");
        // LLM 返回空 JSON → 解析 fallback 到 base 策略
        assertEquals("规则策略：先了解现状再补KYC缺口", result.visitStrategy());
    }

    // ---- fixtures ----

    private static ActivationContract contract(String id, List<String> kiIds) {
        return new ActivationContract("1.0.0", id, "0.1.0", "PRE_VISIT_PREPARATION", "ONTOLOGY_THEN_MAP",
                new ActivationContract.Preconditions(List.of("callerId", "customerId"), List.of("RELATIONSHIP_MANAGER"), true),
                List.of(), kiIds,
                List.of("SQ-CUSTOMER-RELATIONSHIP"), List.of("CLAIM_NOT_FACT"), List.of("SP-02"),
                new ActivationContract.Context(12000, List.of("VERIFIED_FACT"), "CONTRACT_PRIORITY"),
                List.of("HG-B01"), "FAIL_CLOSED");
    }

    private static PrevisitReportContent baseReport() {
        return new PrevisitReportContent(
                "R1-abc12345", "CUST-001", "华东精工装备集团", "王磊",
                "设备融资",
                new PrevisitReportContent.CustomerOverview("制造业", "大型", "战略客户", 500000000L, "中", "技改需求"),
                new PrevisitReportContent.KycGapSummary(List.of("注册资本"), List.of("股东"), List.of("授信需求"), List.of("确认融资额度")),
                List.of(),
                List.of("基础问题1"),
                List.of("基础风险1"),
                "规则策略：先了解现状再补KYC缺口");
    }
}
