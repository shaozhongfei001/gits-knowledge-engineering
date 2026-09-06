package com.gien.gits.api.productknowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 读取 KERT {@code 04_serve/interpretation} 投影快照的适配器。
 *
 * <p>受控失败：快照目录未配置、不可读或产品投影缺失时返回空；
 * 解析异常按「KERT 不可达」处理并抛出 {@link KnowledgeSourceUnavailableException}，
 * 由控制器转为 503，<strong>绝不</strong>本地兜底生成产品结论。</p>
 */
@Component
@ConditionalOnProperty(name = "gits.product-knowledge.enabled", havingValue = "true", matchIfMissing = true)
public class KertReleaseSnapshotAdapter implements ProductKnowledgeInterpretationPort {

    private static final Logger log = LoggerFactory.getLogger(KertReleaseSnapshotAdapter.class);

    private final Path snapshotDir;
    private final ObjectMapper objectMapper;

    public KertReleaseSnapshotAdapter(
            @Value("${gits.product-knowledge.snapshot-dir:}") String snapshotDir,
            ObjectMapper objectMapper) {
        this.snapshotDir = snapshotDir == null || snapshotDir.isBlank() ? null : Path.of(snapshotDir);
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<InterpretationProjection> load(String productId) {
        if (snapshotDir == null) {
            log.warn("[PK-INTERPRETATION] snapshot-dir 未配置，受控失败: productId={}", productId);
            throw new KnowledgeSourceUnavailableException("KERT 解读快照目录未配置");
        }
        Path file = snapshotDir.resolve(productId + ".json");
        if (!Files.isRegularFile(file)) {
            log.info("[PK-INTERPRETATION] 无投影快照: productId={} path={}", productId, file);
            return Optional.empty();
        }
        try {
            InterpretationProjection projection =
                    objectMapper.readValue(Files.readAllBytes(file), InterpretationProjection.class);
            log.info("[PK-INTERPRETATION] 投影加载成功: productId={} releaseId={} lifecycle={}",
                    productId, projection.getReleaseId(), projection.getLifecycleState());
            return Optional.of(projection);
        } catch (Exception ex) {
            log.error("[PK-INTERPRETATION] 投影解析失败，受控失败: productId={} file={}", productId, file, ex);
            throw new KnowledgeSourceUnavailableException("KERT 解读投影不可解析");
        }
    }
}
