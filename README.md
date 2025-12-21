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
