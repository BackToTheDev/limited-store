# Limited Store

## 프로젝트 개요
Limited Store는 한정 수량 상품 주문 시 발생하는 책임 분리, 정합성, 트랜잭션 경계 문제를 실험하기 위한 개인 백엔드 프로젝트입니다.  
모놀리식 구조로 CRUD와 비즈니스 로직을 구현한 뒤,  
테스트와 구조 이해를 중심으로 Order ↔ Product 영역만 최소 수준으로 분리하는 과도기적 MSA 구조를 설계했습니다.

본 프로젝트의 목적은 완성된 MSA가 아니라,  
서비스 분리 기준과 실패 판단을 코드와 테스트로 설명하는 것입니다.

---

## 기술 스택
- Java 17  
- Spring Boot 3.3.5  
- Spring Data JPA  
- Spring Security + JWT  
- JUnit5 / Mockito  
- Spring MVC Test  
- Spring Cloud OpenFeign  
- Swagger (springdoc)

---

## 설계 목표

### 목표 아님
- 완전한 MSA 아키텍처
- API Gateway / Service Discovery / Circuit Breaker
- Saga / 분산 트랜잭션

### 목표
- 서비스 분리 기준을 설명할 수 있는 구조 설계
- Order ↔ Product 간 책임 경계 명확화
- 통신 실패 시 비즈니스 판단 기준 정의
- 확장 가능한 과도기 구조 유지

---

## 테스트 전략
본 프로젝트에서는 계층별 책임을 검증하는 테스트 구조를 사용합니다.

### Service Test
- 비즈니스 규칙 및 도메인 로직 검증
- Repository 및 외부 통신(Feign Client) Mockito Mock 처리
- 검증 항목:
  - 상품 미존재
  - 재고 부족
  - 중복 주문
  - 정상 주문 생성 / 취소

Service Test는 시스템이 어떤 규칙으로 실패해야 하는가를 검증하는 계층입니다.

### Controller Test
- Spring MVC 관점에서 요청/응답 흐름 검증
- @WebMvcTest 기반으로 Controller 계층만 로딩
- Service는 @MockitoBean으로 대체
- JWT 인증은 통과되었다고 가정하고 requestAttr("memberId")로 인증 정보 주입
- GlobalExceptionHandler를 통해 HTTP 상태 코드와 공통 응답 포맷 변환 여부 검증

Controller Test는 시스템이 외부에 실패를 어떻게 표현하는가를 검증합니다.

---

## MSA Transition Structure (Order ↔ Product)

아래 구조는 모놀리식에서 MSA로 전환하는 과도기적 설계 상태를 나타냅니다.  
현재는 상품 존재 여부만 Product 서비스로 분리하고,  
재고 확인 및 차감은 로컬 트랜잭션으로 유지하고 있습니다.

```mermaid
flowchart LR

Client[Client / Frontend]

subgraph Monolith Phase
    OrderService[Order Service]
    ProductRepository[(Product Repository)]
end

subgraph MSA Phase (Transition)
    ProductService[Product Service]
end

Client --> OrderService
OrderService -->|Feign: exists(productId)| ProductService
OrderService -->|Local Transaction| ProductRepository
