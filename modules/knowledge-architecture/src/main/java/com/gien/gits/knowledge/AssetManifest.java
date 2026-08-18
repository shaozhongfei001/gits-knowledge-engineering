package com.gien.gits.knowledge;

import java.util.List;

/**
 * 四类资产清单（Asset Manifest）领域模型，对应合同 CTR-ASSET-001
 * (specs/knowledge-architecture/schemas/asset-manifest.schema.json)。
 *
 * <p>仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record AssetManifest(
        String schemaVersion,
        String assetId,
        String assetType,
        String name,
        String domain,
        String version,
        String status,
        Source source,
        Governance governance,
        List<String> capabilities,
        Activation activation,
        Evidence evidence,
        List<String> relatedAssets,
        List<String> limitations) {

    public AssetManifest {
        capabilities = orEmpty(capabilities);
        relatedAssets = orEmpty(relatedAssets);
        limitations = orEmpty(limitations);
    }

    private static List<String> orEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Source(
            String type,
            String uri,
            String authority,
            String contentMode,
            String sourceVersionPolicy) {}

    public record Governance(
            String owner,
            String classification,
            String permissionInherit,
            List<String> allowedActions) {

        public Governance {
            allowedActions = orEmpty(allowedActions);
        }

        private static List<String> orEmpty(List<String> value) {
            return value == null ? List.of() : List.copyOf(value);
        }
    }

    public record Activation(
            String mode,
            String adapter,
            List<String> requiredParameters,
            Integer maxContextTokens,
            String failurePolicy) {

        public Activation {
            requiredParameters = orEmpty(requiredParameters);
        }

        private static List<String> orEmpty(List<String> value) {
            return value == null ? List.of() : List.copyOf(value);
        }
    }

    public record Evidence(
            boolean citationRequired,
            boolean sourceVersionRequired,
            boolean contentHashRequired) {}
}
