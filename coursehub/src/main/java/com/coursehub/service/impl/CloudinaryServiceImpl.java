package com.coursehub.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.coursehub.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
            );
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded to Cloudinary: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Lỗi upload file lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("Failed to delete file from Cloudinary publicId={}: {}", publicId, e.getMessage());
        }
    }
}
