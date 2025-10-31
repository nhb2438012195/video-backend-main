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
     * 清除指定存储桶中所有未完成的分片上传任务（multipart uploads）
     *
     * @param bucketName 存储桶名称，若为 null 或 blank，则使用默认 bucket
     */
    public void clearAllIncompleteMultipartUploads(String bucketName) {
        String targetBucket = (bucketName == null || bucketName.trim().isEmpty()) ? defaultBucketName : bucketName;

        log.info("开始清理存储桶 [{}] 中所有未完成的分片上传任务...", targetBucket);

        try {
            ListMultipartUploadsRequest request = ListMultipartUploadsRequest.builder()
                    .bucket(targetBucket)
                    .build();

            ListMultipartUploadsResponse response = s3Client.listMultipartUploads(request);
            List<MultipartUpload> uploads = response.uploads();

            if (uploads.isEmpty()) {
                log.info("存储桶 [{}] 中没有未完成的分片上传任务。", targetBucket);
                return;
            }

            log.info("发现 {} 个未完成的分片上传任务，开始中止...", uploads.size());

            for (MultipartUpload upload : uploads) {
                try {
                    AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                            .bucket(targetBucket)
                            .key(upload.key())
                            .uploadId(upload.uploadId())
                            .build();

                    s3Client.abortMultipartUpload(abortRequest);
                    log.info("已中止分片上传任务: key={}, uploadId={}", upload.key(), upload.uploadId());
                } catch (Exception e) {
                    log.error("中止分片上传任务失败: key={}, uploadId={}", upload.key(), upload.uploadId(), e);
                }
            }

            log.info("存储桶 [{}] 的未完成分片上传任务清理完成。", targetBucket);
        } catch (Exception e) {
            log.error("清理存储桶 [{}] 的未完成分片上传任务时发生异常", targetBucket, e);
        }
    }

    /**
     * 初始化分片上传
     *
     * @param objectName 文件名
     * @return 分片上传 ID
     */
    public String  initiateMultipartUpload(String objectName) {
            CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
            String uploadId = response.uploadId();
            log.info("初始化分片上传成功，uploadId: {}, objectName: {}", uploadId, objectName);
            return uploadId;
    }

    /**
     * 上传单个分片
     *
     * @param uploadId    分片上传 ID
     * @param partNumber  分片序号（从 1 开始！）
     * @param file        分片文件（MultipartFile）
     * @return ETag（用于后续合并）
     */
    public String uploadPart(String uploadId, int partNumber, MultipartFile file,String objectName) throws IOException {
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
    }
    /**
     * 完成分片上传（合并所有分片）
     *
     * @param uploadId 分片上传 ID
     * @param partETags 按 partNumber 顺序排列的 ETag 列表（索引 0 对应 part 1）
     */
    public void completeMultipartUpload(String uploadId, List<String> partETags,String objectName) {
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
            log.info("完成分片上传，uploadId: {}, objectName: {}", uploadId, objectName);
    }

    /**
     * 取消分片上传（清理未完成的分片）
     */
    public void abortMultipartUpload(String uploadId,String objectName) {
            if (objectName == null) return;
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(defaultBucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(request);
            log.info("已取消分片上传，uploadId: {}", uploadId);
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