package com.coursehub.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Họ và tên từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[^0-9`~!@#$%^&*()_\\-+=\\[\\]{}|;:',.<>?/\\\\\"]*$",
            message = "Họ tên không được chứa ký tự đặc biệt hoặc chữ số")
    private String fullName;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ (phải là số VN 10 chữ số)")
    private String phoneNumber;

    @Size(max = 500, message = "Tiểu sử tối đa 500 ký tự")
    private String bio;
}
