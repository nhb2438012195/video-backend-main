package com.nhb.service.impl;

import com.nhb.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class CommonServiceImpl implements CommonService {
@Override
    public String checkUserName() {
        String username =null;
        username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名错误:"+ username);
        }
        return username;
    }

    @Override
    public String getUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    /**
     * 删除指定目录下的指定文件夹（包括文件夹本身及其所有内容）
     * @param parentDir 父目录路径
     * @param folderName 要删除的文件夹名称
     * @throws IOException 如果删除失败
     */
    @Override
    public  void deleteFolder(String parentDir, String folderName) throws IOException {
        Path folderToDelete = Paths.get(parentDir, folderName);

        // 检查文件夹是否存在
        if (!Files.exists(folderToDelete)) {
            throw new IOException("要删除的文件夹不存在: " + folderToDelete.toAbsolutePath());
        }

        // 检查是否为目录
        if (!Files.isDirectory(folderToDelete)) {
            throw new IOException("指定路径不是文件夹: " + folderToDelete.toAbsolutePath());
        }

        // 递归删除整个目录树
        Files.walk(folderToDelete)
                .sorted((p1, p2) -> -p1.compareTo(p2)) // 逆序排列，确保先删除子文件再删除父目录
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        System.out.println("已删除: " + path);
                    } catch (IOException e) {
                        System.err.println("删除失败: " + path + " - " + e.getMessage());
                    }
                });
    }
    /**
     * 将 MultipartFile 保存到指定目录
     * @param file 上传的文件
     * @param uploadDir 目标目录路径（如 "uploads/"）
     * @param fileName 保存的文件名（可选，若为 null 则使用原始文件名）
     */
    public  void saveMultipartFile(MultipartFile file, String uploadDir, String fileName) throws IOException {
        // 确保目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 如果未指定文件名，使用原始文件名
        if (fileName == null || fileName.isEmpty()) {
            fileName = file.getOriginalFilename();
        }

        // 防止路径遍历攻击（可选但推荐）
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path filePath = uploadPath.resolve(fileName);

        // 保存文件
        file.transferTo(filePath.toFile());
    }
}
