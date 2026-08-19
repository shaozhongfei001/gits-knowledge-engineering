package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.gien.gits.api.service.report.CrmWritebackService;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;
import com.gien.gits.ontology.RelationshipReport;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 回归测试：前端迭代决策传入的 {@code previousReportId} 可能为 String 报告 ID（如
 * {@code R1-<uuid>}，来自 R1 访前报告），而非 {@link RelationshipReport} 的 UUID 报告 ID。
 * 后端必须容错处理，不得因 {@code UUID.fromString} 抛异常返回 400（修复 P22 后真实系统 bug）。
 */
class ReportGenerationServicePreviousReportIdTest {

    private ReportGenerationService service;
    private WritableRelationshipReportRepository reportRepo;

    @BeforeEach
    void setUp() {
        reportRepo = mock(WritableRelationshipReportRepository.class);
        service = new ReportGenerationService(
                reportRepo,
                mock(WritableCommitmentRepository.class),
                mock(WritableFactReconciliationRepository.class),
                mock(WritableOpportunitySignalRepository.class),
                mock(CustomerContextService.class),
                mock(ContextInheritanceService.class),
                mock(CustomerOperatingViewService.class),
                mock(CrmWritebackService.class),
                mock(LlmClient.class));
    }

    @Test
    void prefixedStringReportIdDoesNotThrow400() {
        // 前端传的 R1 访前报告 ID（带 R1- 前缀，非 UUID）——修复前 UUID.fromString 抛异常
        RelationshipReport result = service.generateUpdatedRelationshipReport(
                "OC-001", "journey-id", "CUST-001",
                "客户CFO确认Q3有5000万设备采购预算", "R1-484eb18c");

        // 不抛异常，正常生成 R7 更新报告
        assertNotNull(result, "prefixed reportId must not cause 400/exception");
        verify(reportRepo, never()).findById(any(UUID.class));
    }

    @Test
    void blankPreviousReportIdBehavesAsNoPrevious() {
        RelationshipReport result = service.generateUpdatedRelationshipReport(
                "OC-001", "journey-id", "CUST-001", "新证据描述", "");

        assertNotNull(result, "blank previousReportId must not throw");
        verify(reportRepo, never()).findById(any(UUID.class));
    }
}
