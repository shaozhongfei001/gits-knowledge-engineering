package com.gien.gits.api.config;

import com.gien.gits.adapter.filesystem.InMemoryActivationContractReader;
import com.gien.gits.adapter.filesystem.InMemoryAssetCatalogReader;
import com.gien.gits.adapter.filesystem.InMemoryKnowledgeElementReader;
import com.gien.gits.adapter.filesystem.InMemoryKnowledgeMapReader;
import com.gien.gits.adapter.filesystem.InMemoryRoutePolicyReader;
import com.gien.gits.adapter.filesystem.KnowledgeSnapshotLoader;
import com.gien.gits.knowledge.plan.DefaultActivationPlanner;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.port.ActivationPlannerPort;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import com.gien.gits.knowledge.port.KnowledgeElementPort;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import com.gien.gits.knowledge.port.RoutePolicyEvaluatorPort;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import com.gien.gits.knowledge.route.DefaultRoutePolicyEvaluator;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识架构控制面装配（P22 E1 生产可用控制面）。
 *
 * <p>启动时通过 {@link KnowledgeSnapshotLoader} 从 Git/文件系统权威源一次性加载
 * {@link InMemoryKnowledgeStore} 内存快照，运行时各 Port 高频读内存（消除 GAP-2 每请求扫盘）。
 * 快照加载失败（目录缺失/为空）按 fail-closed 拒绝启动，不产生半成品控制面。
 *
 * <p>数据根目录配置项：{@code gits.knowledge.root}（默认 {@code specs/knowledge-architecture}）。</p>
 */
@Configuration
public class KnowledgeArchitectureConfig {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeArchitectureConfig.class);

    @Bean
    public InMemoryKnowledgeStore inMemoryKnowledgeStore(
            @Value("${gits.knowledge.root:specs/knowledge-architecture}") String knowledgeRoot) {

        Objects.requireNonNull(knowledgeRoot, "gits.knowledge.root");
        KnowledgeSnapshotLoader loader = new KnowledgeSnapshotLoader(Path.of(knowledgeRoot));
        InMemoryKnowledgeStore store = loader.load();
        log.info("[KnowledgeArchitecture] in-memory snapshot loaded: {} maps, {} assets, {} contracts, "
                        + "{} policies, {} elements",
                store.maps().size(), store.assets().size(), store.contracts().size(),
                store.policies().size(), store.elements().size());
        return store;
    }

    @Bean
    public KnowledgeMapPort knowledgeMapPort(InMemoryKnowledgeStore store) {
        return new InMemoryKnowledgeMapReader(store);
    }

    @Bean
    public AssetCatalogPort assetCatalogPort(InMemoryKnowledgeStore store) {
        return new InMemoryAssetCatalogReader(store);
    }

    @Bean
    public ActivationContractPort activationContractPort(InMemoryKnowledgeStore store) {
        return new InMemoryActivationContractReader(store);
    }

    @Bean
    public RoutePolicyPort routePolicyPort(InMemoryKnowledgeStore store) {
        return new InMemoryRoutePolicyReader(store);
    }

    @Bean
    public KnowledgeElementPort knowledgeElementPort(InMemoryKnowledgeStore store) {
        return new InMemoryKnowledgeElementReader(store);
    }

    @Bean
    public RoutePolicyEvaluatorPort routePolicyEvaluator(
            RoutePolicyPort routePolicyPort,
            @Value("${gits.knowledge.route-policy-id:RP-CORP-RM-001}") String policyId) {
        return new DefaultRoutePolicyEvaluator(routePolicyPort, policyId);
    }

    @Bean
    public ActivationPlannerPort activationPlanner(
            RoutePolicyEvaluatorPort routePolicyEvaluator,
            ActivationContractPort activationContractPort,
            AssetCatalogPort assetCatalogPort,
            KnowledgeMapPort knowledgeMapPort) {
        return new DefaultActivationPlanner(
                routePolicyEvaluator,
                activationContractPort,
                assetCatalogPort,
                knowledgeMapPort,
                Instant::now);
    }
}
