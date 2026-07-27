package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 120, message = "Slug tối đa 120 ký tự")
    private String slug;

    @Size(max = 100, message = "Icon tối đa 100 ký tự")
    private String icon;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    private Long parentId;
}
