package com.supersonic.limitedstore.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .status(200)
            .message("SUCCESS")
            .data(data)
            .build();
    }

    public static ApiResponse<?> error(int status, String message) {
        return ApiResponse.builder()
            .status(status)
            .message(message)
            .data(null)
            .build();
    }
}
