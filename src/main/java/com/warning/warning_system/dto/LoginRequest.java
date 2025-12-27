package com.warning.warning_system.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private Integer username; // 对应各表的 ID (学号/工号)
    private String password;
    private String role; // 1:管理员, 2:学生, 3:辅导员, 4:教师
}