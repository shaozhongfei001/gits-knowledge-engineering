package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.port.ActivationContractPort;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * 文件系统激活合同读取适配器，对应合同 CTR-ACTIVATION-001。
 *
 * <p>从 {@code activations/} 目录加载 {@code <contractId>.json}。
 * 校验 fail-closed：内容不合法一律返回 {@link Optional#empty()}。</p>
 */
public final class FilesystemActivationContractReader implements ActivationContractPort {

    private final Path activationsDir;
    private final FailClosedJsonReader reader;

    public FilesystemActivationContractReader(Path activationsDir) {
        this.activationsDir = Objects.requireNonNull(activationsDir, "activationsDir");
        this.reader = new FailClosedJsonReader();
    }

    @Override
    public Optional<ActivationContract> find(String contractId) {
        if (!PathSafety.isSafeSegment(contractId)) {
            return Optional.empty();
        }
        Path file = activationsDir.resolve(contractId + ".json");
        if (!PathSafety.isWithinBase(activationsDir, file) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        return reader.read(file, ActivationContract.class, this::isValid);
    }

    /** 必需字段校验（fail-closed）：contractId、version、taskType、routeMode、preconditions、context、failurePolicy。 */
    private boolean isValid(ActivationContract contract) {
        if (contract == null) {
            return false;
        }
        return hasText(contract.contractId())
                && hasText(contract.version())
                && hasText(contract.taskType())
                && hasText(contract.routeMode())
                && contract.preconditions() != null
                && contract.context() != null
                && hasText(contract.failurePolicy());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
