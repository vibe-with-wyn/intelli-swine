package com.spmss.intelliswine.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long farmId;
    private String farmName;
}
