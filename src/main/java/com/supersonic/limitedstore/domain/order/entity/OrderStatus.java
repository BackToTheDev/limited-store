package com.supersonic.limitedstore.domain.order.entity;

public enum OrderStatus {
    READY, // 주문 생성
    PAID, // 결제 완료
    CANCELLED, // 주문 취소
}
