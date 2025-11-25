package com.supersonic.limitedstore.common.exception;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(
                e.getErrorCode().getHttpStatus().value(),
                e.getMessage()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
            .status(500)
            .body(ApiResponse.error(500, "Internal Server Error"));
    }
}
