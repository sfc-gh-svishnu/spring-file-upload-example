package com.example.fileupload.stream.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.core.*;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
@RequestMapping("/api/stream")
public class StreamFileUploadController {
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping(value = "/upload")
    public ResponseEntity<String> upload(HttpServletRequest request) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                log.warn("Not a multipart request");
                return ResponseEntity.badRequest()
                        .body("Not a multipart request.");
            }

            FileItemFactory<DiskFileItem> factory = new DiskFileItemFactory.Builder().get();
            JakartaServletFileUpload<DiskFileItem, FileItemFactory<DiskFileItem>> upload =
                    new JakartaServletFileUpload<>(factory);
            StringBuilder uploadedFiles = new StringBuilder();

            FileItemInputIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemInput item = iter.next();
                if (!item.isFormField()) {
                    String originalFilename = item.getName();
                    if (originalFilename != null && !originalFilename.isEmpty()) {
                        String uniqueFileName = System.currentTimeMillis() + "_" + originalFilename;
                        Path filePath = uploadPath.resolve(uniqueFileName);
                        log.info("Processing file: {} -> {}", originalFilename, uniqueFileName);

                        try (InputStream inputStream = item.getInputStream();
                             OutputStream outputStream = Files.newOutputStream(filePath)) {
                            IOUtils.copy(inputStream, outputStream);

                            if (!uploadedFiles.isEmpty()) {
                                uploadedFiles.append(", ");
                            }
                            uploadedFiles.append(uniqueFileName);

                            log.info("Successfully saved file: {}", uniqueFileName);
                        }
                    }
                }
            }

            if (uploadedFiles.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No files were uploaded");
            }

            return ResponseEntity.ok("Successfully uploaded files: " + uploadedFiles);

        } catch (Exception e) {
            log.error("Error processing upload", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to upload files: " + e.getMessage());
        }
    }
}
