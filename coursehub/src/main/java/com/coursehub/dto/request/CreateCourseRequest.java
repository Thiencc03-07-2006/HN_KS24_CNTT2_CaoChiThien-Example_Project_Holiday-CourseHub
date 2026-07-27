package com.coursehub.dto.request;

import com.coursehub.enums.CourseLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Tiêu đề khóa học không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề từ 10 đến 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả ngắn không được để trống")
    @Size(min = 20, max = 500, message = "Mô tả ngắn từ 20 đến 500 ký tự")
    private String shortDescription;

    private String description;

    @NotNull(message = "Giá khóa học không được để trống")
    @DecimalMin(value = "0.00", message = "Giá khóa học phải >= 0")
    private BigDecimal price;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Cấp độ không được để trống")
    private CourseLevel level;

    @Size(max = 50, message = "Ngôn ngữ tối đa 50 ký tự")
    private String language;

    private String thumbnailUrl;
}
