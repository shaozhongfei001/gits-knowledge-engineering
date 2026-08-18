package com.gien.gits.adapter.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 路径越界与符号链接防护（fail-closed）。
 *
 * <p>受控文件读取器仅允许读取仓库内的受控文件：拒绝包含路径分隔符/`.`/`..`/绝对路径的 ID，
 * 并防御性地校验解析后路径仍在基目录内；不跟随指向基目录外的符号链接。</p>
 */
final class PathSafety {

    private PathSafety() {}

    /**
     * 校验 {@code id} 可作为安全的单段文件名。
     *
     * <p>ID 只能作为受控文件库内的单个文件名片段使用，禁止包含路径分隔符（`/`、`\`）、
     * 相对路径片段（`.`、`..`）或绝对路径前缀，从而杜绝路径穿越（path traversal）攻击。</p>
     *
     * @param id 待校验的标识（如 contractId、policyId、mapId）
     * @return true 表示可安全用作单段文件名；false 表示应 fail-closed 拒绝
     */
    static boolean isSafeSegment(String id) {
        // 空值或空白：无合法文件名，直接拒绝。
        if (id == null || id.isBlank()) {
            return false;
        }
        // 含路径分隔符（正斜杠或反斜杠）：试图构造多级/跨目录路径，拒绝。
        if (id.contains("/") || id.contains("\\")) {
            return false;
        }
        // 恰好是当前目录或上级目录：拒绝，防止 `resolve("..")` 越出受控目录。
        if (id.equals(".") || id.equals("..")) {
            return false;
        }
        return true;
    }

    /**
     * 校验解析后的文件仍位于 {@code base} 之内，且（若存在）不是指向基目录外的符号链接。
     *
     * <p>这是第二道防线（深度防御）：即使 ID 通过了 {@link #isSafeSegment(String)} 的字符校验，
     * 这里仍对最终解析路径做规范化比对，并拒绝指向基目录外的符号链接，防止通过符号链接越界读取
     * 仓库外任意文件（如用户目录、环境凭据）。</p>
     *
     * @param base      受控目录（如 maps/、routes/、activations/、assets/）
     * @param candidate 已用 ID 解析出的候选文件路径
     * @return true 表示可安全读取；false 表示应 fail-closed 拒绝
     */
    static boolean isWithinBase(Path base, Path candidate) {
        // 规范化：把相对路径转绝对并解析 `.`/`..`，消除因路径拼接产生的假性"位于目录内"。
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();
        // 第一道：候选路径必须以基目录为前缀，否则越出受控范围。
        if (!normalizedCandidate.startsWith(normalizedBase)) {
            return false;
        }
        // 第二道：拒绝指向基目录外的符号链接（不跟随不可信符号链接）。
        // 注意：这里用 toRealPath 解析符号链接指向的真实路径后再做前缀比对。
        if (Files.isSymbolicLink(candidate)) {
            try {
                Path real = candidate.toRealPath();
                return real.startsWith(normalizedBase);
            } catch (java.io.IOException error) {
                // 无法解析真实路径（链接损坏等）→ 保守拒绝。
                return false;
            }
        }
        return true;
    }
}
