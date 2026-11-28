package com.jpdictionary.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.-]+$",
            message = "Tên đăng nhập chỉ được chứa chữ cái không dấu, số và ký tự _ . -"
    )
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số"
    )
    private String password;

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(min = 2, max = 100, message = "Tên hiển thị phải từ 2 đến 100 ký tự")
    private String displayName;

    @Pattern(
            regexp = "^(?i)(N[1-5])?$",
            message = "Mục tiêu JLPT phải nằm trong khoảng N1 đến N5"
    )
    private String jlptTarget;
}
