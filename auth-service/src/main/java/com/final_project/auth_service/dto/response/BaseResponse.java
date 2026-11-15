package com.final_project.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private Integer statusCode;
    private String message;
    private T data;

    public BaseResponse(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = null;
    }
}
