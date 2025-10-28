package com.nhb.util;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinIOUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;
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