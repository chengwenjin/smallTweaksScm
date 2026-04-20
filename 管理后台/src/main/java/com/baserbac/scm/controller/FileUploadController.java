package com.baserbac.scm.controller;

import com.baserbac.common.result.R;
import com.baserbac.scm.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Tag(name = "文件上传管理")
@RestController
@RequestMapping("/api/scm/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> fileUrls = fileUploadService.uploadFiles(files);
            return R.success(fileUrls);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public R<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        fileUploadService.deleteFile(fileUrl);
        return R.success();
    }
}
