package com.supersonic.limitedstore.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "Stock is empty"),
    DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "Order already exists");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
