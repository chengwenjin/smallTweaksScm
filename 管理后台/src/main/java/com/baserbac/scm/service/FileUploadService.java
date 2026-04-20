package com.baserbac.scm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload-path:uploads/}")
    private String uploadPath;

    @Value("${file.max-size:10485760}")
    private Long maxFileSize;

    private static final String[] ALLOWED_TYPES = {
        "image/jpeg", "image/png", "image/gif", "image/bmp",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    public List<String> uploadFiles(MultipartFile[] files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            return fileUrls;
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullPath = uploadPath + File.separator + datePath;
        
        File targetDir = new File(fullPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException("文件大小超过限制，最大允许" + (maxFileSize / 1024 / 1024) + "MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            String filePath = fullPath + File.separator + newFilename;
            File targetFile = new File(filePath);
            
            file.transferTo(targetFile);
            
            String fileUrl = "/uploads/" + datePath + "/" + newFilename;
            fileUrls.add(fileUrl);
            
            log.info("文件上传成功: {}", fileUrl);
        }

        return fileUrls;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String filePath = uploadPath + File.separator + fileUrl.replace("/uploads/", "").replace("/", File.separator);
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (Exception e) {
            log.warn("文件删除失败: {}", fileUrl, e);
        }
    }
}
