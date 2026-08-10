package com.gien.gits.api.service;

import com.gien.gits.ontology.port.WritableBankRelationshipSnapshotRepository;
import com.gien.gits.ontology.port.WritableCreditFacilityRepository;
import com.gien.gits.ontology.port.WritableCustomerRepository;
import com.gien.gits.ontology.port.WritableGroupRelationshipRepository;
import com.gien.gits.ontology.port.WritableLegalEntityRepository;
import com.gien.gits.ontology.port.WritableTransactionRecordRepository;
import com.gien.gits.ontology.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 客户上下文服务 — 组装 Customer Operating View
 * 聚合: 客户主档 + 法人实体 + 集团关系 + 银行关系快照 + 授信额度 + 交易流水
 */
public class CustomerContextService {

    private final WritableCustomerRepository customerRepo;
    private final WritableLegalEntityRepository legalEntityRepo;
    private final WritableGroupRelationshipRepository groupRelRepo;
    private final WritableBankRelationshipSnapshotRepository bankRelRepo;
    private final WritableCreditFacilityRepository creditFacilityRepo;
    private final WritableTransactionRecordRepository transactionRepo;

    public CustomerContextService(
            WritableCustomerRepository customerRepo,
            WritableLegalEntityRepository legalEntityRepo,
            WritableGroupRelationshipRepository groupRelRepo,
            WritableBankRelationshipSnapshotRepository bankRelRepo,
            WritableCreditFacilityRepository creditFacilityRepo,
            WritableTransactionRecordRepository transactionRepo) {
        this.customerRepo = Objects.requireNonNull(customerRepo);
        this.legalEntityRepo = Objects.requireNonNull(legalEntityRepo);
        this.groupRelRepo = Objects.requireNonNull(groupRelRepo);
        this.bankRelRepo = Objects.requireNonNull(bankRelRepo);
        this.creditFacilityRepo = Objects.requireNonNull(creditFacilityRepo);
        this.transactionRepo = Objects.requireNonNull(transactionRepo);
    }

    /**
     * 组装客户经营视图 — RM工作台核心数据
     */
    @Transactional(readOnly = true)
    public CustomerOperatingView buildOperatingView(String customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        List<LegalEntity> entities = legalEntityRepo.findByGroupId(customerId);
        List<GroupRelationship> groupRels = groupRelRepo.findByGroupId(customerId);
        Optional<BankRelationshipSnapshot> bankRel = bankRelRepo.findLatestByCustomerId(customerId);
        List<CreditFacility> creditFacilities = creditFacilityRepo.findByCustomerId(customerId);
        List<TransactionRecord> transactions = transactionRepo.findByCustomerId(customerId);

        return new CustomerOperatingView(
            customer, entities, groupRels, bankRel, creditFacilities, transactions);
    }

    public Optional<Customer> findCustomer(String customerId) {
        return customerRepo.findById(customerId);
    }

    public List<Customer> findCustomersByRm(String rmId) {
        if ("ALL".equalsIgnoreCase(rmId)) {
            return customerRepo.findAll();
        }
        return customerRepo.findByRmId(rmId);
    }

    public void saveCustomer(Customer customer) {
        customerRepo.save(customer);
    }

    public void saveLegalEntity(LegalEntity entity) {
        legalEntityRepo.save(entity);
    }

    public void saveGroupRelationship(GroupRelationship rel) {
        groupRelRepo.save(rel);
    }

    public void saveBankRelationshipSnapshot(BankRelationshipSnapshot snapshot) {
        bankRelRepo.save(snapshot);
    }

    public void saveCreditFacility(CreditFacility facility) {
        creditFacilityRepo.save(facility);
    }

    public void saveTransactionRecord(TransactionRecord record) {
        transactionRepo.save(record);
    }

    /**
     * 客户经营视图 — 聚合所有客户上下文数据
     */
    public record CustomerOperatingView(
        Customer customer,
        List<LegalEntity> entities,
        List<GroupRelationship> groupRelationships,
        Optional<BankRelationshipSnapshot> bankRelationship,
        List<CreditFacility> creditFacilities,
        List<TransactionRecord> transactions) {}
}
