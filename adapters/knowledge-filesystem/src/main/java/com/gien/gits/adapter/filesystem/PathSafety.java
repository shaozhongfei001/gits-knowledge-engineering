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
     * 校验 {@code id} 可作为安全的单段文件名（不允许任何路径分隔符、相对路径片段或绝对路径）。
     */
    static boolean isSafeSegment(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        if (id.contains("/") || id.contains("\\")) {
            return false;
        }
        if (id.equals(".") || id.equals("..")) {
            return false;
        }
        return true;
    }

    /**
     * 校验解析后的文件仍位于 {@code base} 之内，且（若存在）不是指向基目录外的符号链接。
     * 返回 true 表示可安全读取。
     */
    static boolean isWithinBase(Path base, Path candidate) {
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();
        if (!normalizedCandidate.startsWith(normalizedBase)) {
            return false;
        }
        // 拒绝指向基目录外的符号链接（不跟随不可信符号链接）。
        if (Files.isSymbolicLink(candidate)) {
            try {
                Path real = candidate.toRealPath();
                return real.startsWith(normalizedBase);
            } catch (java.io.IOException error) {
                return false;
            }
        }
        return true;
    }
}
