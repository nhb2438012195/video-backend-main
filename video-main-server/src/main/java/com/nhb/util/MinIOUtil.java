package com.nhb.util;


import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class MinIOUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    /**
     * 创建存储桶
     */
//    public void createBucket(String bucketName) throws Exception {
//        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//        }
//    }

    /**
     * 获取所有存储桶
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /**
     * 上传文件
     */
//    public  String uploadFile(MultipartFile file) throws Exception {
//          String  bucketName = defaultBucketName;
//
//        createBucket(bucketName);
//
//        String originalFilename = file.getOriginalFilename();
//        // 生成唯一文件名，避免重复
//        String fileName = System.currentTimeMillis() + "_" + originalFilename;
//
//        minioClient.putObject(
//                PutObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(fileName)
//                        .stream(file.getInputStream(), file.getSize(), -1)
//                        .contentType(file.getContentType())
//                        .build()
//        );
//
//        return fileName;
//    }
//    public String uploadFile(MultipartFile file) throws Exception {
//        String bucketName = defaultBucketName;
//        createBucket(bucketName);
//
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null || originalFilename.trim().isEmpty()) {
//            originalFilename = "unnamed";
//        }
//
//        String baseName;
//        String extension = "";
//        int lastDot = originalFilename.lastIndexOf('.');
//        if (lastDot > 0) {
//            extension = originalFilename.substring(lastDot);
//            baseName = originalFilename.substring(0, lastDot);
//        } else {
//            baseName = originalFilename;
//        }
//
//        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]+", "_");
//        baseName = baseName.replaceAll("_+", "_");
//        baseName = baseName.replaceAll("^_|_$", "");
//        if (baseName.isEmpty()) {
//            baseName = "file";
//        }
//
//        String fileName = System.currentTimeMillis() + "_" + RandomStringUtils.randomAlphanumeric(6) + "_" + baseName + extension;
//
//        try (InputStream inputStream = file.getInputStream()) {
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(fileName)
//                            .stream(inputStream, file.getSize(), -1)
//                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
//                            .build()
//            );
//        }
//
//        return fileName; // 返回存储的文件名
//    }
    /**
     * 上传文件到 MinIO 并返回存储的对象名称
     * @param file 上传的 MultipartFile 文件
     * @return 返回在 MinIO 中存储的唯一对象名称（如：1733113200000_image.jpg）
     * @throws Exception 若上传失败
     */
    public String uploadFile(MultipartFile file) throws Exception {
        String bucketName = defaultBucketName;


        // 获取原始文件名，若为空则使用默认名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "unnamed";
        }

        String baseName;
        String extension = "";

        // 分离文件名和扩展名（只取最后一个点）
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot); // 包含 "."
            baseName = originalFilename.substring(0, lastDot);
        } else {
            baseName = originalFilename;
        }

        // 清理文件名：只保留中英文、数字，其他替换为下划线
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]+", "_");
        // 去除首尾一个或多个下划线
        baseName = baseName.replaceAll("^_+|_+$", "");
        // 如果清理后为空，则使用默认名
        if (baseName.isEmpty()) {
            baseName = "file";
        }

        // 生成唯一文件名：时间戳 + 清理后的文件名 + 扩展名
        String fileName = System.currentTimeMillis() + "_" + baseName + extension;

        // 上传文件到 MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );
        } catch (IOException e) {
            throw new Exception("文件读取失败: " + e.getMessage(), e);
        } catch (ErrorResponseException e) {
            throw new Exception("MinIO 服务响应错误: " + e.getMessage(), e);
        } catch (InsufficientDataException e) {
            throw new Exception("上传数据不足: " + e.getMessage(), e);
        } catch (InternalException e) {
            throw new Exception("MinIO 内部错误: " + e.getMessage(), e);
        } catch (InvalidResponseException e) {
            throw new Exception("MinIO 响应无效: " + e.getMessage(), e);
        } catch (ServerException e) {
            throw new Exception("MinIO 服务器错误: " + e.getMessage(), e);
        } catch (XmlParserException e) {
            throw new Exception("XML 解析失败: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new Exception("加密签名异常: " + e.getMessage(), e);
        }

        return fileName; // 返回 MinIO 中存储的对象名称
    }
    /**
     * 下载文件
     */
    public InputStream downloadFile(String fileName) throws Exception {

         String   bucketName = defaultBucketName;

        
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
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
     * 获取文件访问链接
     */
    public String getPresignedUrl(String fileName)  {
        return getPresignedUrl(fileName, 60);
    }
    public String getPresignedUrl(String fileName, int expiry)  {

       String    bucketName = defaultBucketName;

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expiry, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO错误:获取文件访问链接失败"+e);
        }
    }
    /**
     * 从 MinIO 带签名的 URL 中提取图片文件名
     * @param minioSignedUrl MinIO 生成的预签名图片 URL（如 http://xxx:9000/sky/xxx.jpg?X-Amz-Algorithm=...）
     * @return 提取到的文件名（如 1758619434443_illust_125139031_20250918_123833.jpg）
     * @throws Exception 当 URL 格式非法或解析失败时抛出异常
     */
    public  String extractFileNameFromMinioUrl(String minioSignedUrl){
        // 1. 校验入参，避免空指针
        if (Objects.isNull(minioSignedUrl) || minioSignedUrl.trim().isEmpty()) {
            throw new RuntimeException("MinIO错误:MinIO 预签名 URL 不能为空");
        }

        // 2. 解析 URL，获取“协议://域名/路径”部分（去掉问号后的签名参数）
        URL url = null;
        try {
            url = new URL(minioSignedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("MinIO错误:"+e);
        }
        String path = url.getPath(); // 此时 path 格式为“/存储桶名/文件名”（如 /sky/1758619434443_illust_125139031_20250918_123833.jpg）

        // 3. 校验路径格式（确保包含存储桶名和文件名，即至少有两个“/”）
        if (path == null || path.split("/").length < 3) {
            throw new RuntimeException("MinIO错误:MinIO URL 格式非法，路径部分不符合预期：" + path);
        }

        // 4. 提取最后一个“/”后的内容（即文件名）
        int lastSlashIndex = path.lastIndexOf("/");
        String fileName = path.substring(lastSlashIndex + 1); // 从“/”的下一个字符开始截取

        // 5. 校验文件名（避免空文件名或仅包含特殊字符）
        if (fileName.trim().isEmpty()) {
            throw new RuntimeException("MinIO错误:从 URL 中提取的文件名为空，请检查 URL 格式");
        }

        return fileName;
    }

}