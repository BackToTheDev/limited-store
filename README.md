# Limited Store

한정 수량 상품 주문 과정에서 발생할 수 있는 재고 정합성 문제와 트랜잭션 경계를 설계 관점에서 검증하기 위한 개인 백엔드 프로젝트입니다.

이 프로젝트의 목적은 완성된 MSA 구현이 아니라,  
서비스를 언제 분리해야 하는지와 재고 변경이 있는 도메인을 어디까지 하나의 트랜잭션으로 묶어야 하는지를 코드와 테스트로 설명하는 것입니다.

---

## 1. 프로젝트 개요

Limited Store는 한정 수량 상품 주문 시 발생할 수 있는 다음 문제를 다룹니다.

- 재고 변경이 있는 도메인의 데이터 정합성
- 주문 생성과 재고 차감의 원자성 보장
- 서비스 분리 기준 설정
- 예외를 API 응답으로 일관되게 변환하는 구조

모놀리식 기반으로 주문과 재고 로직을 구현한 후,  
상품 존재 여부 확인만 외부 서비스로 분리한 과도기적 구조를 설계했습니다.

---

## 2. 기술 스택

- Java 17
- Spring Boot 3.3.5
- Spring Data JPA
- Spring Security + JWT
- Spring Cloud OpenFeign
- JUnit5 / Mockito
- Spring MVC Test
- PostgreSQL / H2
- Docker

---

## 3. 핵심 설계

### 3.1 서비스 분리 기준

상품 존재 여부 확인은 외부 Product Service로 분리했습니다.

```java
if (!productClient.exists(productId)) {
    throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
}
```

존재 여부는 상태 변경이 없는 읽기 성격의 책임이기 때문에  
분리 비용이 낮다고 판단했습니다.

반면, 재고는 상태 변경이 발생하는 영역이므로  
현재 단계에서는 주문 생성과 함께 로컬 트랜잭션으로 묶었습니다.

---

### 3.2 트랜잭션 경계

주문 생성 로직은 `@Transactional`로 묶여 있습니다.

```java
@Transactional
public ApiResponse<OrderResponseDto> createOrder(UUID memberId, OrderRequestDto dto)
```

주문 생성 과정은 다음 순서로 진행됩니다.

1. 상품 존재 여부 확인 (외부 서비스)
2. 재고 확인
3. 중복 주문 확인
4. 주문 생성
5. 재고 차감
6. 주문 이벤트 로그 저장

이 중 하나라도 예외가 발생하면 전체 작업은 롤백됩니다.

이를 통해 다음을 보장합니다.

- 재고 음수 방지
- 주문만 생성되고 재고가 차감되지 않는 상태 방지
- 부분 성공 데이터 차단

---

### 3.3 재고 감소 로직

재고는 엔티티 내부에서 직접 감소시키며,  
0 이하일 경우 예외를 발생시킵니다.

```java
public void decreaseStock() {
    if (this.stock <= 0) {
        throw new CustomException(ErrorCode.OUT_OF_STOCK);
    }
    this.stock -= 1;
}
```

### 3.4 예외 처리 구조

도메인 예외는 `CustomException`으로 정의하고,  
`GlobalExceptionHandler`를 통해 일관된 API 응답으로 변환합니다.

```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<?> handleCustomException(CustomException e) {
    // ...
}
```

`ErrorCode`는 HTTP 상태 코드와 메시지를 함께 정의합니다.

예:

- `PRODUCT_NOT_FOUND` → 404
- `OUT_OF_STOCK` → 400
- `ALREADY_PURCHASED` → 409

이를 통해 내부 도메인 예외와 외부 API 계약을 분리했습니다.

---

## 4. Failure Flow

### 상품 미존재
- Product Service에서 존재 여부 false
- `PRODUCT_NOT_FOUND` 예외 발생
- HTTP 404 반환

### 재고 부족
- 재고 수량이 0 이하
- `OUT_OF_STOCK` 예외 발생
- HTTP 400 반환

### 중복 주문
- 동일 회원이 동일 상품을 재주문
- `ALREADY_PURCHASED` 예외 발생
- HTTP 409 반환

### 트랜잭션 내부 실패
- 주문 저장 또는 재고 저장 중 예외 발생
- 전체 롤백

---

## 5. 테스트 전략

### Service Test
- 비즈니스 규칙 단위 검증
  - 상품 미존재
  - 재고 부족
  - 중복 주문
  - 정상 주문
- 예외 발생 시 롤백 검증

Repository 및 Feign Client는 Mock 처리하여  
도메인 로직에 집중합니다.

### Controller Test
- API 계약 검증
- `@WebMvcTest` 기반 테스트
- Service는 `@MockitoBean`으로 대체
- `GlobalExceptionHandler` 기반 상태 코드 검증
- JWT는 `requestAttr("memberId")`로 인증 가정

---

## 6. 현재 한계

- 재고는 로컬 트랜잭션 기반으로 처리
- 동시성 제어 전략은 별도 구현하지 않음
- Feign 통신 실패에 대한 세부 재시도 정책은 적용하지 않음

향후 확장 시 다음을 고려할 수 있습니다.

- 재고 서비스 분리
- 동시성 제어 전략 적용
- 서비스 간 실패 정책 명문화

---

## 7. 실행 방법

```bash
./gradlew test
./gradlew bootRun
