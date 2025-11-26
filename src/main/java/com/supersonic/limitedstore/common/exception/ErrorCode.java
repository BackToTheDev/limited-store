package com.supersonic.limitedstore.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "중복되는 이메일 입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "중복되는 닉네임 입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "Stock is empty"),
    DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "Order already exists"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 패스워드 입니다." ),;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
