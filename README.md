# Limited Store

## 프로젝트 개요
Limited Store는 한정 수량 상품 주문을 가정한 개인 백엔드 프로젝트입니다.
모놀리식 구조로 CRUD와 비즈니스 로직을 구현한 뒤,
테스트와 구조 이해를 중심으로 최소 수준의 MSA 분리를 경험하는 것을 목표로 했습니다.

## 기술 스택
- Java 17
- Spring Boot
- Spring Data JPA
- JUnit5 / Mockito
- Spring MVC Test

## 테스트 전략
본 프로젝트에서는 계층별 책임을 명확히 하기 위해 테스트를 분리했습니다.

### Service Test
- 비즈니스 로직과 규칙 검증
- Mockito를 사용하여 Repository 의존성 Mock 처리
- 성공/실패 케이스를 통해 도메인 규칙을 검증

### Controller Test
- Spring MVC 관점에서 요청/응답 흐름 검증
- @WebMvcTest 기반으로 Controller 계층만 로딩
- Service는 @MockitoBean으로 대체
- JWT 인증 필터는 통과되었다고 가정하고 requestAttr로 인증 정보를 주입
- Service에서 발생한 예외가 GlobalExceptionHandler를 통해
  HTTP 상태 코드와 공통 응답 포맷으로 변환되는지 검증

## Why Order ↔ Product Only(MSA Transition)
본 프로젝트는 완전한 MSA 구현이 목적이 아니라,
서비스 분리 기준과 책임 경계를 설명할 수 있는 구조를 만드는 것을 목표로 했습니다.

### 왜 Order ↔ Product만 분리했는가
- 주문 생성 시 Order의 상품의 존재 여부와 재고 상태에 의존합니다.
- 이에 따라 상품의 존재 여부 판단 책임을 Product 서비스로 분리했습니다.

### 현재 구조 (과도기 상태)
- 상품 존재 여부: Product 서비스 책임 (Feign Client 통신)
- 재고 확인 및 차감: 모놀리식 트랜젝션 유지

이는 모놀리식에서 MSA로 전환하는 과도기적 구조로,
재고 차감 API 분리 시 Product 엔티티 및 Repository 의존을 제거할 수 있도록
구조를 의도적으로 열어두었습니다.

### 목표가 아닌 것
- 완전한 MSA 구성
- Gateway, Eureka 등 인프라 구성
- 분산 트랜젝션 처리
