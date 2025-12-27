package com.warning.warning_system.dto;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResult {
    private String token;
    private String name;
    private Integer role;
}