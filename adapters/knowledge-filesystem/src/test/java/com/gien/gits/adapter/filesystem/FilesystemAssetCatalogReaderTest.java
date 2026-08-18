package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.AssetManifest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemAssetCatalogReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void listAllLoadsAssetsAcrossTypeDirs() throws IOException {
        Path assetsDir = Files.createDirectory(tempDir.resolve("assets"));
        Files.createDirectory(assetsDir.resolve("foundational-data"));
        Files.createDirectory(assetsDir.resolve("process-tools"));
        Files.writeString(assetsDir.resolve("foundational-data/customer-profile.md"), ASSET_CUSTOMER, StandardCharsets.UTF_8);
        Files.writeString(assetsDir.resolve("process-tools/sp05.md"), ASSET_SP05, StandardCharsets.UTF_8);

        FilesystemAssetCatalogReader reader = new FilesystemAssetCatalogReader(assetsDir);
        List<AssetManifest> all = reader.listAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(a -> a.assetId().equals("ASSET-DATA-CUSTOMER-PROFILE")));
        assertTrue(all.stream().anyMatch(a -> a.assetId().equals("ASSET-TOOL-SP05")));
    }

    @Test
    void listByDomainFiltersCorrectly() throws IOException {
        Path assetsDir = Files.createDirectory(tempDir.resolve("assets2"));
        Files.createDirectory(assetsDir.resolve("foundational-data"));
        Files.writeString(assetsDir.resolve("foundational-data/a.md"), ASSET_CUSTOMER, StandardCharsets.UTF_8);

        FilesystemAssetCatalogReader reader = new FilesystemAssetCatalogReader(assetsDir);
        assertEquals(1, reader.listByDomain("KD-CORP-RM").size());
        assertTrue(reader.listByDomain("KD-OTHER").isEmpty());
    }

    @Test
    void findReturnsMatchingAsset() throws IOException {
        Path assetsDir = Files.createDirectory(tempDir.resolve("assets3"));
        Files.createDirectory(assetsDir.resolve("foundational-data"));
        Files.writeString(assetsDir.resolve("foundational-data/a.md"), ASSET_CUSTOMER, StandardCharsets.UTF_8);

        FilesystemAssetCatalogReader reader = new FilesystemAssetCatalogReader(assetsDir);
        assertTrue(reader.find("ASSET-DATA-CUSTOMER-PROFILE").isPresent());
        assertTrue(reader.find("ASSET-UNKNOWN").isEmpty());
        assertTrue(reader.find(null).isEmpty());
    }

    @Test
    void listAllSkipsMalformedAssetFailClosed() throws IOException {
        Path assetsDir = Files.createDirectory(tempDir.resolve("assets4"));
        Files.createDirectory(assetsDir.resolve("foundational-data"));
        Files.writeString(assetsDir.resolve("foundational-data/good.md"), ASSET_CUSTOMER, StandardCharsets.UTF_8);
        Files.writeString(assetsDir.resolve("foundational-data/bad.md"), "---\n{not-json}\n---", StandardCharsets.UTF_8);
        Files.writeString(assetsDir.resolve("foundational-data/missing.md"), "---\n{\"assetId\":\"ASSET-X\"}\n---", StandardCharsets.UTF_8);

        FilesystemAssetCatalogReader reader = new FilesystemAssetCatalogReader(assetsDir);
        List<AssetManifest> all = reader.listAll();

        assertEquals(1, all.size());
        assertEquals("ASSET-DATA-CUSTOMER-PROFILE", all.get(0).assetId());
    }

    @Test
    void listAllReturnsEmptyWhenDirMissing() {
        FilesystemAssetCatalogReader reader = new FilesystemAssetCatalogReader(tempDir.resolve("nope"));
        assertTrue(reader.listAll().isEmpty());
    }

    private static final String ASSET_CUSTOMER = """
            ---
            {"schemaVersion":"1.0.0","assetId":"ASSET-DATA-CUSTOMER-PROFILE","assetType":"FOUNDATIONAL_DATA","name":"客户画像","domain":"KD-CORP-RM","version":"0.1.0","status":"VALIDATION","source":{"type":"FILESYSTEM_MOCK","uri":"scenario_data/customer.json","authority":"SYNTHETIC","contentMode":"RUNTIME_FETCH"},"governance":{"owner":"客户主数据Owner","classification":"SENSITIVE","permissionInherit":"CALLER","allowedActions":["READ","QUERY"]},"capabilities":["获取客户信息"],"activation":{"mode":"QUERY","adapter":"filesystem-customer-adapter","requiredParameters":["customerId"],"maxContextTokens":1800,"failurePolicy":"FAIL_CLOSED"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true}}
            ---
            """;

    private static final String ASSET_SP05 = """
            ---
            {"schemaVersion":"1.0.0","assetId":"ASSET-TOOL-SP05","assetType":"PROCESS_TOOL","name":"SP-05","domain":"KD-CORP-RM","version":"1.1.0","status":"VALIDATION","source":{"type":"SKILL_DESCRIPTOR","uri":"skills/SP-05.json","authority":"REFERENCE","contentMode":"EXECUTE"},"governance":{"owner":"KYC知识Owner","classification":"INTERNAL","permissionInherit":"CALLER","allowedActions":["EXECUTE"]},"capabilities":["输出Unknown"],"activation":{"mode":"EXECUTE","adapter":"skill-runtime-adapter","requiredParameters":["activationPlanId"],"maxContextTokens":0,"failurePolicy":"DEGRADE_WITH_DISCLOSURE"},"evidence":{"citationRequired":true,"sourceVersionRequired":true,"contentHashRequired":true}}
            ---
            """;
}
