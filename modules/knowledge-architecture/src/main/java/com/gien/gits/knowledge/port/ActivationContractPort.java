package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.ActivationContract;
import java.util.Optional;

/**
 * 激活合同读取 Port（CTR-ACTIVATION-001 消费者：activation_planner）。
 *
 * <p>契约返回 {@link Optional}：未找到或内容不合法（fail-closed）时返回 {@link Optional#empty()}。</p>
 */
public interface ActivationContractPort {

    Optional<ActivationContract> find(String contractId);
}
