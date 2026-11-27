package com.jpdictionary.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @JsonAlias({"email", "username"})
    @NotBlank(message = "Vui lòng nhập tên đăng nhập hoặc email")
    private String identifier;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;
}

