package com.gien.gits.ontology;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 交易流水 — 记录客户账户的资金进出明细
 *
 * @param id                   主键
 * @param transactionId        交易流水号
 * @param customerId           客户ID
 * @param accountId            账户ID
 * @param transactionType      交易类型
 * @param amount               交易金额
 * @param currency             币种
 * @param counterparty         交易对手
 * @param counterpartyIndustry 交易对手行业
 * @param description          交易描述
 * @param transactionDate      交易日期
 * @param createdAt            创建时间
 */
public record Transaction(
        UUID id,
        String transactionId,
        String customerId,
        String accountId,
        TransactionType transactionType,
        BigDecimal amount,
        String currency,
        String counterparty,
        String counterpartyIndustry,
        String description,
        LocalDate transactionDate,
        Instant createdAt) {

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT,
        LOAN_DISBURSE, LOAN_REPAY, TRADE_SETTLEMENT, FEE
    }
}
