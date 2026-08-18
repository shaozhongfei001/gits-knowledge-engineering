package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.RoutePolicy;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * 文件系统路由策略读取适配器，对应合同 CTR-ROUTE-001。
 *
 * <p>从 {@code routes/} 目录加载 {@code <policyId>.json}。
 * 校验 fail-closed：内容不合法一律返回 {@link Optional#empty()}。</p>
 */
public final class FilesystemRoutePolicyReader implements RoutePolicyPort {

    private final Path routesDir;
    private final FailClosedJsonReader reader;

    public FilesystemRoutePolicyReader(Path routesDir) {
        this.routesDir = Objects.requireNonNull(routesDir, "routesDir");
        this.reader = new FailClosedJsonReader();
    }

    @Override
    public Optional<RoutePolicy> find(String policyId) {
        if (!PathSafety.isSafeSegment(policyId)) {
            return Optional.empty();
        }
        Path file = routesDir.resolve(policyId + ".json");
        if (!PathSafety.isWithinBase(routesDir, file) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        return reader.read(file, RoutePolicy.class, this::isValid);
    }

    /** 必需字段校验（fail-closed）：policyId、version、defaultMode、defaultDecision、rules。 */
    private boolean isValid(RoutePolicy policy) {
        if (policy == null) {
            return false;
        }
        return hasText(policy.policyId())
                && hasText(policy.version())
                && hasText(policy.defaultMode())
                && hasText(policy.defaultDecision())
                && policy.rules() != null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
