package com.gien.gits.knowledge.repository;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.RoutePolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存知识快照（不可变），P22 E1 生产可用控制面数据层。
 *
 * <p>启动时由 {@code KnowledgeSnapshotLoader} 从 Git/文件系统权威源一次性填充，
 * 运行时常驻内存供各 Port 高频读取，避免每请求扫描磁盘（GAP-2）。
 * 快照一经构建不可修改（fail-closed：加载失败则拒绝启动）。</p>
 */
public final class InMemoryKnowledgeStore {

    private final Map<String, KnowledgeMap> maps;
    private final Map<String, AssetManifest> assets;
    private final Map<String, ActivationContract> contracts;
    private final Map<String, RoutePolicy> policies;
    private final Map<String, KnowledgeElement> elements;

    private InMemoryKnowledgeStore(Builder builder) {
        this.maps = Map.copyOf(builder.maps);
        this.assets = Map.copyOf(builder.assets);
        this.contracts = Map.copyOf(builder.contracts);
        this.policies = Map.copyOf(builder.policies);
        this.elements = Map.copyOf(builder.elements);
    }

    public Map<String, KnowledgeMap> maps() {
        return maps;
    }

    public Map<String, AssetManifest> assets() {
        return assets;
    }

    public Map<String, ActivationContract> contracts() {
        return contracts;
    }

    public Map<String, RoutePolicy> policies() {
        return policies;
    }

    public Map<String, KnowledgeElement> elements() {
        return elements;
    }

    /** 根知识地图（mapType=ROOT）或为空。 */
    public KnowledgeMap rootMap() {
        return maps.values().stream()
                .filter(map -> "ROOT".equals(map.mapType()))
                .findFirst()
                .orElse(null);
    }

    /** 构建器：快照装载入口，填充后必须 {@link #build()}。 */
    public static final class Builder {

        private final Map<String, KnowledgeMap> maps = new HashMap<>();
        private final Map<String, AssetManifest> assets = new HashMap<>();
        private final Map<String, ActivationContract> contracts = new HashMap<>();
        private final Map<String, RoutePolicy> policies = new HashMap<>();
        private final Map<String, KnowledgeElement> elements = new HashMap<>();

        public Builder putMap(KnowledgeMap map) {
            if (map != null && map.mapId() != null) {
                maps.put(map.mapId(), map);
            }
            return this;
        }

        public Builder putAsset(AssetManifest asset) {
            if (asset != null && asset.assetId() != null) {
                assets.put(asset.assetId(), asset);
            }
            return this;
        }

        public Builder putContract(ActivationContract contract) {
            if (contract != null && contract.contractId() != null) {
                contracts.put(contract.contractId(), contract);
            }
            return this;
        }

        public Builder putPolicy(RoutePolicy policy) {
            if (policy != null && policy.policyId() != null) {
                policies.put(policy.policyId(), policy);
            }
            return this;
        }

        public Builder putElement(KnowledgeElement element) {
            if (element != null && element.elementId() != null) {
                elements.put(element.elementId(), element);
            }
            return this;
        }

        public InMemoryKnowledgeStore build() {
            return new InMemoryKnowledgeStore(this);
        }

        /** 当前已填充条目数（用于加载断言）。 */
        public int size() {
            return maps.size() + assets.size() + contracts.size() + policies.size() + elements.size();
        }

        /** 校验是否加载到非空数据（空快照按 fail-closed 视为无效）。 */
        public boolean isNonEmpty() {
            return size() > 0;
        }

        private Builder() {
            // 私有构造：仅静态工厂使用
        }
    }

    /** 创建空构建器。 */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "InMemoryKnowledgeStore{maps=" + maps.size() + ", assets=" + assets.size()
                + ", contracts=" + contracts.size() + ", policies=" + policies.size()
                + ", elements=" + elements.size() + "}";
    }
}
