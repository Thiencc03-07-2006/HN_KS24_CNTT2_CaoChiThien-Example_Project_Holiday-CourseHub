package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BecomeInstructorRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 80, message = "Tiêu đề giảng viên từ 10 đến 80 ký tự")
    private String headline;

    @NotBlank(message = "Tiểu sử chuyên môn không được để trống")
    @Size(min = 100, message = "Tiểu sử chuyên môn tối thiểu 100 ký tự")
    private String detailedBio;

    @Size(max = 255, message = "Website URL tối đa 255 ký tự")
    private String websiteUrl;

    @Size(max = 255, message = "LinkedIn URL tối đa 255 ký tự")
    private String linkedinUrl;
}
