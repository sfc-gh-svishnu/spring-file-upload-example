package com.example.fileupload.multipart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@RestController
@Slf4j
@RequestMapping("/api/multipart")
public class MultipartFileUploadController {
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(MultipartHttpServletRequest request) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {

                Files.createDirectories(uploadPath);
            }

            Iterator<String> fileNames = request.getFileNames();
            StringBuilder uploadedFiles = new StringBuilder();

            while (fileNames.hasNext()) {
                String fileName = fileNames.next();
                MultipartFile file = request.getFile(fileName);

                if (file != null && !file.isEmpty()) {
                    String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(uniqueFileName);

                    Files.copy(file.getInputStream(), filePath);

                    if (!uploadedFiles.isEmpty()) {
                        uploadedFiles.append(", ");
                    }
                    uploadedFiles.append(uniqueFileName);
                }
            }

            if (!uploadedFiles.isEmpty()) {
                return ResponseEntity.ok("Successfully uploaded files: " + uploadedFiles);
            } else {
                return ResponseEntity.badRequest().body("No files were uploaded");
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to upload files: " + e.getMessage());
        }
    }
}
