package com.coursehub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.coursehub.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService Unit Tests")
public class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @Test
    @DisplayName("uploadFile_success — uploads file and returns secure_url")
    void uploadFile_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        byte[] content = "test content".getBytes();
        given(file.getBytes()).willReturn(content);
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.upload(eq(content), any(Map.class))).willReturn(Map.of("secure_url", "https://cloudinary.com/image.png"));

        String result = cloudinaryService.uploadFile(file, "avatars");

        assertThat(result).isEqualTo("https://cloudinary.com/image.png");
    }

    @Test
    @DisplayName("uploadFile_failure — throws RuntimeException on IOException")
    void uploadFile_failure() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getBytes()).willThrow(new IOException("Read error"));

        assertThatThrownBy(() -> cloudinaryService.uploadFile(file, "avatars"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lỗi upload file");
    }

    @Test
    @DisplayName("deleteFile_success — deletes resource by public id")
    void deleteFile_success() throws IOException {
        given(cloudinary.uploader()).willReturn(uploader);

        cloudinaryService.deleteFile("public-id-123");

        verify(uploader).destroy(eq("public-id-123"), any(Map.class));
    }

    @Test
    @DisplayName("deleteFile_exception — catch and log exception")
    void deleteFile_exception() throws IOException {
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.destroy(anyString(), any(Map.class))).willThrow(new IOException("Delete error"));

        // Should not propagate exception
        cloudinaryService.deleteFile("public-id-123");

        verify(uploader).destroy(eq("public-id-123"), any(Map.class));
    }
}
