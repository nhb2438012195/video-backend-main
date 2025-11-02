package com.nhb.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CommonService {
     String checkUserName();

     String getUserName();

     void deleteFolder(String parentDir, String folderName) throws IOException;
     void saveMultipartFile(MultipartFile file, String uploadDir, String fileName) throws IOException;
}
