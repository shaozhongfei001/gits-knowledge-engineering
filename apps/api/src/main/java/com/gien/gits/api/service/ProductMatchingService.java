package com.gien.gits.api.service;

import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.Transaction;
import com.gien.gits.ontology.Transaction.TransactionType;
import com.gien.gits.ontology.port.CustomerRepository;
import com.gien.gits.ontology.port.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 产品匹配服务 — 基于交易流水+客户特征智能匹配金融产品
 */
public class ProductMatchingService {

    private static final Logger log = LoggerFactory.getLogger(ProductMatchingService.class);

    private static final BigDecimal LARGE_TRADE_THRESHOLD = new BigDecimal("5000000");
    private static final BigDecimal CASH_FLOW_VOLATILITY_THRESHOLD = new BigDecimal("0.3");

    private final TransactionRepository transactionRepo;
    private final CustomerRepository customerRepo;
    private final KycInsightService kycInsightService;

    public ProductMatchingService(TransactionRepository transactionRepo,
                                   CustomerRepository customerRepo,
                                   KycInsightService kycInsightService) {
        this.transactionRepo = transactionRepo;
        this.customerRepo = customerRepo;
        this.kycInsightService = kycInsightService;
    }

    /**
     * 基于交易流水+客户特征匹配产品
     *
     * @param customerId 客户ID
     * @return 匹配的产品推荐列表
     */
    public List<ProductMatch> matchProducts(String customerId) {
        log.info("Matching products for customer: {}", customerId);

        List<ProductMatch> matches = new ArrayList<>();

        var customerOpt = customerRepo.findById(customerId);
        if (customerOpt.isEmpty()) {
            log.warn("Customer not found: {}", customerId);
            return matches;
        }
        Customer customer = customerOpt.get();

        List<Transaction> recentTxns = transactionRepo.findRecentByCustomerId(customerId, 100);
        if (recentTxns.isEmpty()) {
            log.info("No transactions found for customer: {}", customerId);
            return matches;
        }

        // 规则1: 大额贸易结算 → 供应链融资
        if (hasLargeTradeSettlement(recentTxns)) {
            matches.add(new ProductMatch(
                "PROD-SUPPLY-CHAIN", "供应链融资",
                "检测到大额贸易结算交易，建议供应链融资方案",
                calculateConfidence(recentTxns, TransactionType.TRADE_SETTLEMENT),
                "TRADE_SETTLEMENT_DETECTED"));
        }

        // 规则2: 定期贷款还款+新增融资需求 → 续贷/增信
        if (hasRegularLoanRepayment(recentTxns) && hasFinancingNeed(customer)) {
            matches.add(new ProductMatch(
                "PROD-RENEW-CREDIT", "续贷/增信",
                "检测到定期贷款还款且存在新增融资需求，建议续贷或增信方案",
                calculateConfidence(recentTxns, TransactionType.LOAN_REPAY),
                "LOAN_REPAY_AND_NEED"));
        }

        // 规则3: 季度性现金流波动 → 流动资金贷款
        if (hasCashFlowVolatility(recentTxns)) {
            matches.add(new ProductMatch(
                "PROD-WORKING-CAPITAL", "流动资金贷款",
                "检测到季度性现金流波动，建议流动资金贷款以平滑资金需求",
                0.75,
                "CASH_FLOW_VOLATILITY"));
        }

        // 规则4: 高新技术企业+研发投入 → 科技贷
        if (isHighTechCustomer(customer) && hasRdSpending(recentTxns)) {
            matches.add(new ProductMatch(
                "PROD-TECH-LOAN", "科技贷",
                "高新技术企业检测到研发投入，建议科技贷方案",
                0.8,
                "HIGH_TECH_RD"));
        }

        // 规则5: 出口贸易结算 → 贸易融资
        if (hasExportTradeSettlement(recentTxns)) {
            matches.add(new ProductMatch(
                "PROD-TRADE-FINANCE", "贸易融资",
                "检测到出口贸易结算交易，建议贸易融资方案",
                0.7,
                "EXPORT_TRADE"));
        }

        matches.sort(Comparator.comparingDouble(ProductMatch::confidence).reversed());
        log.info("Matched {} products for customer: {}", matches.size(), customerId);
        return matches;
    }

    private boolean hasLargeTradeSettlement(List<Transaction> txns) {
        return txns.stream()
            .filter(t -> t.transactionType() == TransactionType.TRADE_SETTLEMENT)
            .anyMatch(t -> t.amount().compareTo(LARGE_TRADE_THRESHOLD) >= 0);
    }

    private boolean hasRegularLoanRepayment(List<Transaction> txns) {
        long loanRepayCount = txns.stream()
            .filter(t -> t.transactionType() == TransactionType.LOAN_REPAY)
            .count();
        return loanRepayCount >= 3;
    }

    private boolean hasFinancingNeed(Customer customer) {
        // 基于行业和规模判断融资需求
        return customer.industry() != null &&
            (customer.industry().name().contains("MANUFACTURING") ||
             customer.enterpriseScale() == com.gien.gits.ontology.EnterpriseScale.LARGE ||
             customer.enterpriseScale() == com.gien.gits.ontology.EnterpriseScale.MEDIUM);
    }

    private boolean hasCashFlowVolatility(List<Transaction> txns) {
        Map<Integer, BigDecimal> monthlyTotals = new LinkedHashMap<>();
        for (Transaction t : txns) {
            int monthKey = t.transactionDate().getYear() * 100 + t.transactionDate().getMonthValue();
            monthlyTotals.merge(monthKey, t.amount(), BigDecimal::add);
        }
        if (monthlyTotals.size() < 3) return false;

        BigDecimal avg = monthlyTotals.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(monthlyTotals.size()), 2, java.math.RoundingMode.HALF_UP);

        if (avg.compareTo(BigDecimal.ZERO) == 0) return false;

        BigDecimal maxDeviation = monthlyTotals.values().stream()
            .map(v -> v.subtract(avg).abs())
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return maxDeviation.divide(avg, 4, java.math.RoundingMode.HALF_UP)
            .compareTo(CASH_FLOW_VOLATILITY_THRESHOLD) > 0;
    }

    private boolean isHighTechCustomer(Customer customer) {
        // 基于行业判断高新技术企业
        return customer.industry() != null &&
            (customer.industry().name().contains("TECH") ||
             customer.industry().name().contains("IT") ||
             customer.industry().name().contains("ELECTRONICS"));
    }

    private boolean hasRdSpending(List<Transaction> txns) {
        return txns.stream()
            .anyMatch(t -> t.description() != null &&
                (t.description().contains("研发") || t.description().contains("技术")));
    }

    private boolean hasExportTradeSettlement(List<Transaction> txns) {
        return txns.stream()
            .filter(t -> t.transactionType() == TransactionType.TRADE_SETTLEMENT)
            .anyMatch(t -> t.description() != null &&
                (t.description().contains("出口") || t.description().contains("海外") || t.description().contains("跨境")));
    }

    private double calculateConfidence(List<Transaction> txns, TransactionType type) {
        long typeCount = txns.stream().filter(t -> t.transactionType() == type).count();
        double ratio = (double) typeCount / txns.size();
        return Math.min(0.95, 0.5 + ratio * 2);
    }

    /**
     * 产品匹配结果
     *
     * @param productId   产品ID
     * @param productName 产品名称
     * @param reason      匹配原因
     * @param confidence  匹配置信度 (0-1)
     * @param signal      匹配信号
     */
    public record ProductMatch(
            String productId,
            String productName,
            String reason,
            double confidence,
            String signal) {}
}
