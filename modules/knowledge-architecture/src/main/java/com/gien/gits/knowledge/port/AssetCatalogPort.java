package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.AssetManifest;
import java.util.List;
import java.util.Optional;

/**
 * 资产清单读取 Port（CTR-ASSET-001 消费者：asset_registry）。
 *
 * <p>单个查找契约返回 {@link Optional}：未找到或内容不合法（fail-closed）时返回
 * {@link Optional#empty()}；列表接口返回空数组，不返回 {@code null}。</p>
 */
public interface AssetCatalogPort {

    Optional<AssetManifest> find(String assetId);

    List<AssetManifest> listByDomain(String domain);

    List<AssetManifest> listAll();
}
