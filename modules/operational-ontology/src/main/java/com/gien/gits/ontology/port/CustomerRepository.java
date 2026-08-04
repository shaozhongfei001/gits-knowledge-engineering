package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Customer;

import java.util.List;
import java.util.Optional;

/**
 * 客户仓储端口 — 只读操作。
 *
 * <p>定义对 {@link Customer} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableCustomerRepository}。</p>
 */
public interface CustomerRepository {

    /**
     * 根据客户ID查找客户。
     *
     * @param customerId 客户唯一标识
     * @return 找到的客户，若不存在则返回空
     */
    Optional<Customer> findById(String customerId);

    /**
     * 根据客户经理ID查找其名下所有客户。
     *
     * @param rmId 客户经理ID
     * @return 该客户经理名下的客户列表
     */
    List<Customer> findByRmId(String rmId);
}
