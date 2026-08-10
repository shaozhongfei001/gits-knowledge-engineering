package com.gien.gits.ontology.port;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 场景数据提供者端口 — 抽象数据源（文件系统 / classpath / 远程）
 * <p>
 * V1.1引入SCENARIO_DATA_ROOT机制：通过配置指向外部场景数据目录，
 * 实现版本切换（V1.0 classpath → V1.1 filesystem）而无需重新构建。
 * <p>
 * 数据格式支持：JSON, JSONL, CSV, YAML
 */
public interface ScenarioDataProvider {

    /**
     * 获取数据提供者标识（如 "filesystem", "classpath"）
     */
    String getProviderType();

    /**
     * 检查指定路径的资源是否存在
     *
     * @param relativePath 相对于数据根目录的路径，如 "02_master_data/customer_master.json"
     */
    boolean exists(String relativePath);

    /**
     * 以InputStream方式读取资源
     *
     * @param relativePath 相对于数据根目录的路径
     * @return 资源输入流，不存在则返回empty
     */
    Optional<InputStream> openStream(String relativePath);

    /**
     * 以文本方式读取整个资源
     *
     * @param relativePath 相对于数据根目录的路径
     * @return 文件全文内容，不存在则返回empty
     */
    Optional<String> readText(String relativePath);

    /**
     * 按行读取资源（适用于CSV/JSONL）
     *
     * @param relativePath 相对于数据根目录的路径
     * @return 行列表，不存在则返回空列表
     */
    List<String> readLines(String relativePath);

    /**
     * 列出指定目录下的资源文件名
     *
     * @param relativeDir 相对于数据根目录的目录路径
     * @return 文件名列表（不含目录前缀）
     */
    List<String> listFiles(String relativeDir);

    /**
     * 获取数据根目录描述（用于日志/诊断）
     */
    String getRootDescription();
}
