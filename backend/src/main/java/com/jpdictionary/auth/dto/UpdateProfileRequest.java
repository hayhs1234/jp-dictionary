package com.jpdictionary.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(min = 2, max = 100, message = "Tên hiển thị phải từ 2 đến 100 ký tự")
    private String displayName;

    @Pattern(regexp = "^N[1-5]$", message = "Mục tiêu JLPT phải nằm trong khoảng N1 đến N5")
    private String jlptTarget;

    @Size(max = 255, message = "Giới thiệu bản thân tối đa 255 ký tự")
    private String bio;
}

