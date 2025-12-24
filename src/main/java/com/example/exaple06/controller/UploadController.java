package com.example.exaple06.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UploadController {

    // THỬ: Dùng đường dẫn tuyệt đối với debug
    private final String UPLOAD_DIR = "D:/exaple06/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== DEBUG UPLOAD START ===");
            System.out.println("📁 UPLOAD_DIR: " + UPLOAD_DIR);
            
            // Debug current working directory
            Path currentPath = Paths.get("").toAbsolutePath();
            System.out.println("📁 Current working dir: " + currentPath.toString());

            System.out.println("📄 File name: " + file.getOriginalFilename());
            System.out.println("📦 File size: " + file.getSize());
            System.out.println("🔧 Content type: " + file.getContentType());

            if (file.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File rỗng");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Chỉ chấp nhận file ảnh");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Tạo tên file unique
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Tạo thư mục uploads
            Path uploadPath = Paths.get(UPLOAD_DIR);
            System.out.println("📁 Upload path (absolute): " + uploadPath.toAbsolutePath());
            System.out.println("📁 Upload path exists: " + Files.exists(uploadPath));
            
            if (!Files.exists(uploadPath)) {
                System.out.println("🛠️ Creating directory...");
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created upload directory: " + uploadPath.toAbsolutePath());
            }

            // Kiểm tra quyền ghi
            System.out.println("🔐 Is writable: " + Files.isWritable(uploadPath));

            // Lưu file
            Path filePath = uploadPath.resolve(fileName);
            System.out.println("💾 Saving to: " + filePath.toAbsolutePath());
            
            file.transferTo(filePath.toFile());

            System.out.println("✅ File saved successfully!");
            System.out.println("=== DEBUG UPLOAD SUCCESS ===");

            // Trả về response
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "Upload thành công");
            response.put("filePath", "/uploads/" + fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Upload error: " + e.getMessage());
            System.out.println("❌ Error type: " + e.getClass().getName());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi upload: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}