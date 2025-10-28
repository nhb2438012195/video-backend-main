package com.nhb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class S3Util {

    @Autowired
    private S3Client s3Client;

    @Autowired
    @Qualifier("defaultBucketName")
    private String defaultBucketName;


    /**
     * 初始化分片上传
     *
     * @param objectName 文件名
     * @return 分片上传 ID
     */
    public String  initiateMultipartUpload(String objectName) {
        try {
            CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
            String uploadId = response.uploadId();
            log.info("初始化分片上传成功，uploadId: {}, objectName: {}", uploadId, objectName);
            return uploadId;
        } catch (Exception e) {
            log.error("初始化分片上传失败，objectName: {}", objectName, e);
            throw new RuntimeException("初始化分片上传失败", e);
        }
    }

    /**
     * 上传单个分片
     *
     * @param uploadId    分片上传 ID
     * @param partNumber  分片序号（从 1 开始！）
     * @param file        分片文件（MultipartFile）
     * @return ETag（用于后续合并）
     */
    public String uploadPart(String uploadId, int partNumber, MultipartFile file,String objectName) {
        try {
            if (objectName == null) {
                throw new IllegalArgumentException("无效的 uploadId: " + uploadId);
            }

            // 获取文件大小（用于指定 RequestBody 的长度）
            long contentLength = file.getSize();

            UploadPartRequest request = UploadPartRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            // 使用 file.getInputStream() 作为数据源，并指定准确的 contentLength
            UploadPartResponse response = s3Client.uploadPart(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), contentLength)
            );

            String eTag = response.eTag();
            log.debug("上传分片成功，uploadId: {}, part: {}, ETag: {}", uploadId, partNumber, eTag);
            return eTag;
        } catch (IOException e) {
            log.error("读取分片文件失败，uploadId: {}, part: {}", uploadId, partNumber, e);
            throw new RuntimeException("读取分片文件失败", e);
        } catch (Exception e) {
            log.error("上传分片失败，uploadId: {}, part: {}", uploadId, partNumber, e);
            throw new RuntimeException("上传分片失败", e);
        }
    }
    /**
     * 完成分片上传（合并所有分片）
     *
     * @param uploadId 分片上传 ID
     * @param partETags 按 partNumber 顺序排列的 ETag 列表（索引 0 对应 part 1）
     */
    public void completeMultipartUpload(String uploadId, List<String> partETags,String objectName) {
        try {
            if (objectName == null) {
                throw new IllegalArgumentException("无效的 uploadId: " + uploadId);
            }

            List<CompletedPart> completedParts = new ArrayList<>();
            for (int i = 0; i < partETags.size(); i++) {
                completedParts.add(
                        CompletedPart.builder()
                                .partNumber(i + 1) // partNumber 从 1 开始
                                .eTag(partETags.get(i))
                                .build()
                );
            }

            CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();

            s3Client.completeMultipartUpload(request);
            uploadIdToFile.remove(uploadId); // 清理缓存
            log.info("完成分片上传，uploadId: {}, objectName: {}", uploadId, objectName);
        } catch (Exception e) {
            log.error("完成分片上传失败，uploadId: {}", uploadId, e);
            throw new RuntimeException("完成分片上传失败", e);
        }
    }

    /**
     * 取消分片上传（清理未完成的分片）
     */
    public void abortMultipartUpload(String uploadId,String objectName) {
        try {
            if (objectName == null) return;
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(request);
            uploadIdToFile.remove(uploadId);
            log.info("已取消分片上传，uploadId: {}", uploadId);
        } catch (Exception e) {
            log.warn("取消分片上传时出错，uploadId: {}", uploadId, e);
        }
    }

    // ==================== 其他常用方法（可选）====================

    public void putObject(String objectName, InputStream inputStream, long size) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(defaultBucketName)
                            .key(objectName)
                            .build(),
                    RequestBody.fromInputStream(inputStream, size)
            );
        } catch (Exception e) {
            log.error("上传对象失败: {}", objectName, e);
            throw new RuntimeException("上传失败", e);
        }
    }

    public void deleteObject(String objectName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(defaultBucketName)
                .key(objectName)
                .build());
    }
}