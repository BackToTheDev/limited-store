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
- Docker / Docker Compose

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

### 3.5 동시성 제어

동시 주문 환경에서 재고 정합성이 깨지는 문제를 확인하고,
낙관적 락을 적용하여 해결했습니다.

**문제 재현**
- 테스트 조건: 재고 10개 / 동시 요청 100개
- 낙관적 락 미적용 시 최종 재고 4 (6개 차감 유실)

<img width="688" height="533" alt="스크린샷 2026-04-01 130714" src="https://github.com/user-attachments/assets/097e1411-eea8-4fc7-a5a5-ba26572dac24" />


**해결**
Product 엔티티에 `@Version` 필드를 추가하여 낙관적 락을 적용했습니다.

JPA는 UPDATE 쿼리에 version 조건을 자동으로 추가하며,
충돌 발생 시 `OptimisticLockException`이 발생하고 트랜잭션이 롤백됩니다.
```java
@Version
private Long version;
```

- 낙관적 락 적용 후 최종 재고 0 (정합성 보장)

<img width="656" height="462" alt="스크린샷 2026-04-01 130915" src="https://github.com/user-attachments/assets/c622a767-f872-4728-b70f-c45876eacc1a" />

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

## 6. 의도된 설계 선택 및 향후 확장성

현재 구조는 완전한 MSA가 아닌, 모놀리식 기반에서 점진적으로 분리해나가는 과도기적 구조를 전제로 설계했습니다.

데이터 정합성을 최우선으로 고려하여 주문 생성과 재고 차감을 하나의 로컬 트랜잭션으로 처리했습니다.

이는 분산 환경에서 발생할 수 있는 복잡도를 줄이고, 비즈니스 실패를 명확하게 관리하기 위한 의도된 선택입니다.

현재는 다음과 같은 부분을 의도적으로 단순화했습니다.

- 재고는 로컬 트랜잭션 기반 처리
- Feign 통신 실패 재시도 정책 미적용

향후 확장 시에는 다음과 같은 방향을 고려하고 있습니다.

- 재고 서비스 분리
- Saga 패턴 또는 Outbox 패턴 기반 분산 트랜잭션 처리
- 비관적 락 / 분산 락을 통한 동시성 제어
- 서비스 간 실패 정책 명문화

---

## 7. 실행 방법

Docker Compose를 사용하여 애플리케이션과 PostgreSQL을 함께 실행합니다.

```bash
docker compose up -d --build
```
- `-d` 옵션을 통해 컨테이너를 백그라운드로 실행
- 애플리케이션과 DB를 동일 네트워크에서 연결
  
로그 확인
```bash
docker compose logs -f
```

---

## 8. 실행 검증
- Docker Compose로 앱 컨테이너와 PostgreSQL 컨테이너 동시 실행
- 컨테이너 간 네트워크를 통한 DB 연결 확인
- Spring Boot 애플리케이션 정상 구동 확인
- Swagger UI에서 API 호출 성공 확인

애플리케이션과 DB 컨테이너가 독립된 네트워크 환경에서 정상적으로 실행된 것을 확인했습니다.
  <img width="618" height="98" alt="image" src="https://github.com/user-attachments/assets/082967eb-afb1-490e-873b-9329ec4429ad" />


정의된 API 계약에 따라 회원가입 로직이 정상적으로 수행되고, 200 SUCCESS 응답이 반환되는 것을 확인했습니다.
  <img width="1767" height="418" alt="image" src="https://github.com/user-attachments/assets/eaefa007-d854-4dc9-bdf8-3bea375c657f" />
  <img width="1757" height="247" alt="image" src="https://github.com/user-attachments/assets/1bfa6dbd-9187-4092-b230-1984b8489bab" />
