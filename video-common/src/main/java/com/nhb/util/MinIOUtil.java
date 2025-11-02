package com.nhb.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Component
public class MinIOUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;
    /**
     * 上传本地目录下的所有文件到 MinIO 指定 bucket，并以该目录名作为对象前缀。
     *
     * @param bucketName   MinIO 的 bucket 名称
     * @param localDirPath 本地目录路径，例如 "E:/123"
     * @throws Exception 上传过程中可能抛出的异常
     */
    public void uploadDirectory(String bucketName, String localDirPath) throws Exception {
        Path localDir = Paths.get(localDirPath);
        if (!Files.exists(localDir) || !Files.isDirectory(localDir)) {
            throw new IllegalArgumentException("本地路径不存在或不是一个目录: " + localDirPath);
        }

        String dirName = localDir.getFileName().toString(); // 获取最后一级目录名，如 "123"

        try (Stream<Path> walk = Files.walk(localDir)) {
            walk.filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // 计算相对路径，例如 localDir 是 E:/123，file 是 E:/123/a/b.txt，则 relative 是 a/b.txt
                            Path relativePath = localDir.relativize(file);
                            // 拼接对象名：123/a/b.txt
                            String objectName = dirName + "/" + relativePath.toString().replace("\\", "/");

                            log.info("正在上传文件: {} 到 bucket: {}, 对象名: {}", file, bucketName, objectName);

                            minioClient.putObject(
                                    PutObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(objectName)
                                            .stream(Files.newInputStream(file), file.toFile().length(), -1)
                                            .build()
                            );

                            log.info("上传成功: {}", objectName);
                        } catch (Exception e) {
                            log.error("上传文件失败: {}", file, e);
                            throw new RuntimeException("上传文件失败: " + file, e);
                        }
                    });
        } catch (IOException e) {
            log.error("遍历本地目录失败: {}", localDirPath, e);
            throw new RuntimeException("遍历本地目录失败", e);
        }
    }

    /**
     * 上传文件到 MinIO 并返回存储的对象名称
     */
    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, "");
    }

    public String uploadFile(MultipartFile file, String prefixName) throws Exception {
        String bucketName = defaultBucketName;
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new Exception("上传文件失败：原始文件名为空");
        }

        if (!prefixName.isEmpty() && org.apache.commons.lang3.StringUtils.isNotBlank(prefixName)) {
            originalFilename = prefixName + originalFilename;
        }

        String baseName;
        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
            baseName = originalFilename.substring(0, lastDot);
        } else {
            baseName = originalFilename;
        }

        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]+", "_");
        baseName = baseName.replaceAll("^_+|_+$", "");
        if (baseName.isEmpty()) {
            baseName = "file";
        }

        String fileName = System.currentTimeMillis() + "_" + baseName + extension;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new Exception("文件上传失败: " + e.getMessage(), e);
        }
        return fileName;
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(defaultBucketName)
                        .object(fileName)
                        .build()
        );
    }
    /**
     * 从 MinIO 下载文件并保存到本地指定路径
     *
     * @param fileName     MinIO 中的对象名（如 "videos/123.mp4"）
     * @param localPath    本地保存的完整路径（如 "E:/videoMp4/input.mp4"）
     * @throws Exception
     */
    public void downloadFileToLocal(String fileName, String localPath) throws Exception {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(defaultBucketName)
                        .object(fileName)
                        .build())) {

            Path targetPath = Paths.get(localPath);
            // 自动创建父目录（如 E:/videoMp4/ 不存在会自动创建）
            Files.createDirectories(targetPath.getParent());

            // 将输入流复制到目标文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ 文件已下载到: {}", localPath);
        }
    }
    /**
     * 删除文件
     */
    public void deleteFile(String fileName, String bucketName) throws Exception {
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = defaultBucketName;
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 获取预签名 URL
     */
    public String getPresignedUrl(String fileName) {
        return getPresignedUrl(fileName, 60);
    }

    public String getPresignedUrl(String fileName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(defaultBucketName)
                            .object(fileName)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: {}", fileName, e);
            throw new RuntimeException("MinIO错误: 获取文件访问链接失败", e);
        }
    }

    /**
     * 从 MinIO 预签名 URL 中提取文件名
     */
    public String extractFileNameFromMinioUrl(String minioSignedUrl) {
        if (minioSignedUrl == null || minioSignedUrl.trim().isEmpty()) {
            throw new RuntimeException("MinIO错误: MinIO 预签名 URL 不能为空");
        }

        URL url;
        try {
            url = new URL(minioSignedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("MinIO错误: URL 格式非法", e);
        }

        String path = url.getPath();
        if (path == null || path.split("/").length < 3) {
            throw new RuntimeException("MinIO错误: URL 路径格式非法: " + path);
        }

        int lastSlashIndex = path.lastIndexOf("/");
        String fileName = path.substring(lastSlashIndex + 1);
        if (fileName.trim().isEmpty()) {
            throw new RuntimeException("MinIO错误: 提取的文件名为空");
        }

        return fileName;
    }

    // ================== 分片上传 API（MinIO SDK 8.5.12 兼容） ==================

}