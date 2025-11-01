package com.nhb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class FFmpegUtils {

    private static final Logger log = LoggerFactory.getLogger(FFmpegUtils.class);

    private static final String BIN_DIR = "bin";
    private static final ConcurrentHashMap<String, Path> extractedBinaries = new ConcurrentHashMap<>();
    private static final Path tempBaseDir = createTempBaseDir();

    /**
     * 获取 FFmpeg 可执行文件路径（自动解压）
     */
    public Path getFfmpegExecutable() throws IOException {
        String os = detectOS();
        String binaryName = os.equals("windows") ? "ffmpeg.exe" : "ffmpeg";

        return extractedBinaries.computeIfAbsent(os, key -> {
            try {
                Path tempDir = Files.createTempDirectory(tempBaseDir, "ffmpeg-");
                Path targetBinary = tempDir.resolve(binaryName);

                String resourcePath = String.format("/%s/%s/%s", BIN_DIR, os, binaryName);
                try (InputStream is = FFmpegUtils.class.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        throw new IOException("未在 classpath 中找到 FFmpeg 二进制: " + resourcePath);
                    }
                    Files.copy(is, targetBinary, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!os.equals("windows")) {
                    setExecutable(targetBinary);
                }

                log.info("FFmpeg 已解压到临时目录: {}", targetBinary);
                return targetBinary;
            } catch (IOException e) {
                log.error("解压 FFmpeg 失败", e);
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * 执行 FFmpeg 命令（带超时和日志）
     *
     * @param command FFmpeg 参数列表（不含 ffmpeg 命令本身）
     * @param timeoutSeconds 超时时间（秒），建议 300（5分钟）
     * @throws IOException 执行失败或超时
     */
    private void execute(List<String> command, int timeoutSeconds, Path workingDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) {
            pb.directory(workingDir.toFile()); // 设置工作目录
        }
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // 读取 stdout 和 stderr（避免死锁）
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread outThread = readStream(process.getInputStream(), stdout);
        Thread errThread = readStream(process.getErrorStream(), stderr);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        // 等待读取线程结束
        outThread.join(1000);
        errThread.join(1000);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("FFmpeg 执行超时（" + timeoutSeconds + "秒）");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorMsg = stderr.toString().trim();
            log.error("FFmpeg 执行失败，退出码: {}, 命令: {}", exitCode, command);
            log.error("FFmpeg 错误输出:\n{}", errorMsg);
            throw new IOException("FFmpeg 转换失败，退出码: " + exitCode + "\n" + errorMsg);
        }

        log.debug("FFmpeg 执行成功，输出:\n{}", stdout.toString().trim());
    }

    private Thread readStream(InputStream inputStream, StringBuilder output) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                // 忽略
            }
        });
        thread.start();
        return thread;
    }

    /**
     * 快捷方法：MP4 转 DASH（无转码）
     */
    public void convertMp4ToDash(Path inputMp4, Path outputDir, int segmentDurationSeconds)
            throws IOException, InterruptedException {
        if (!Files.exists(inputMp4)) {
            throw new FileNotFoundException("输入 MP4 不存在: " + inputMp4);
        }

        Path optimizedMp4 = optimizeMp4ForStreaming(inputMp4);
        try {
            Files.createDirectories(outputDir);
            Path mpdFile = outputDir.resolve("manifest.mpd");

            String ffmpegPath = getFfmpegExecutable().toAbsolutePath().toString();

            List<String> args = Arrays.asList(
                    ffmpegPath,
                    "-i", optimizedMp4.toAbsolutePath().toString(),
                    "-c", "copy",
                    "-f", "dash",
                    "-seg_duration", String.valueOf(segmentDurationSeconds),
                    "-window_size", "10",
                    mpdFile.toAbsolutePath().toString()  // 注意：这里只写文件名，因为工作目录是 outputDir
            );

            // ✅ 关键：在 outputDir 目录下执行命令
            execute(args, 300, outputDir);

            // 验证 .m4s 是否生成
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDir, "*.m4s")) {
                if (!stream.iterator().hasNext()) {
                    throw new IOException("未生成 .m4s 分片文件");
                }
            }

            long mpdSize = Files.size(mpdFile);
            if (mpdSize < 200) {
                throw new IOException("DASH 转换失败：manifest.mpd 太小");
            }

            log.info("✅ DASH 转换成功，输出目录: {}", outputDir);
        } finally {
            Files.deleteIfExists(optimizedMp4);
        }
    }
    // --- 内部工具方法 ---
    /**
     * 优化 MP4：将 moov atom 移到文件开头，使其支持流式播放和 DASH 分片
     */
    public Path optimizeMp4ForStreaming(Path inputMp4) throws IOException, InterruptedException {
        if (!Files.exists(inputMp4)) {
            throw new FileNotFoundException("输入文件不存在: " + inputMp4);
        }

        String fileName = inputMp4.getFileName().toString();
        String optimizedName = fileName.replaceFirst("\\.mp4$", "_optimized.mp4");
        Path optimizedMp4 = inputMp4.getParent().resolve(optimizedName);

        // ✅ 获取实际 ffmpeg 路径
        String ffmpegPath = getFfmpegExecutable().toAbsolutePath().toString();

        List<String> args = Arrays.asList(
                ffmpegPath,  // ← 替换 "ffmpeg"
                "-i", inputMp4.toAbsolutePath().toString(),
                "-c", "copy",
                "-movflags", "+faststart",
                optimizedMp4.toAbsolutePath().toString()
        );

        execute(args, 300,inputMp4.getParent());

        if (!Files.exists(optimizedMp4)) {
            throw new IOException("MP4 优化失败，未生成输出文件");
        }

        return optimizedMp4;
    }
    private static String detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return "windows";
        if (osName.contains("mac")) return "macos";
        if (osName.contains("nux") || osName.contains("aix")) return "linux";
        throw new UnsupportedOperationException("不支持的操作系统: " + osName);
    }

    private static void setExecutable(Path file) throws IOException {
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(file);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(file, perms);
        } catch (UnsupportedOperationException e) {
            // 非 POSIX 系统（如某些 Windows 子系统），忽略
            log.warn("无法设置可执行权限（非 POSIX 系统）: {}", file);
        }
    }

    private static Path createTempBaseDir() {
        try {
            Path dir = Files.createTempDirectory("ffmpeg-root-");
            // 注册 JVM 关闭钩子统一清理
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    deleteRecursively(dir);
                } catch (IOException ignored) {
                }
            }));
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建 FFmpeg 临时根目录", e);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path child : stream) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    // Spring 容器销毁时清理（双重保险）
    @PreDestroy
    public void cleanup() {
        try {
            deleteRecursively(tempBaseDir);
        } catch (IOException e) {
            log.warn("清理 FFmpeg 临时目录失败", e);
        }
    }
}