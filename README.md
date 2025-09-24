# Andamiro Dashboard

안다미로 대시보드는 **해양 사고 대응 SaaS 플랫폼**의 백엔드 대시보드 애플리케이션입니다.  
선주와 선원이 사고 현장(예: 유류 유출) 관련 이미지/영상을 업로드하고, 이를 기반으로 보고서와 보험 클레임 프로세스를 효율화할 수 있도록 지원합니다.  

---

## 🚢 주요 기능
- 사고 등록 및 상세 조회
- 선주/선원별 권한에 따른 증거 자료 업로드
- 선주 전용 보고서 생성 및 보험 클레임 보조
- 대응 가이드(Response Guide) 조회 및 매핑
- 추후 AI 기반 영상 분석 기능 연동 예정

---

## ⚙️ 기술 스택
- **Backend**: Spring Boot 3.x (Java 17+)
- **DB**: MySQL 8.x
- **ORM**: Spring Data JPA
- **API 문서화**: SpringDoc OpenAPI (Swagger UI)
- **빌드 도구**: Gradle 8.x
- **테스트**: JUnit5 + Mockito + MockMvc
- **CI/CD**: GitHub Actions (예정)

---

## 🚀 실행 방법
```bash
# 로컬 DB 준비
docker run --name andamiro-db \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=dashboard \
  -p 3306:3306 -d mysql:8

# 애플리케이션 실행
./gradlew bootRun
Swagger UI는 실행 후 아래 주소에서 확인 가능합니다.
👉 http://localhost:8080/swagger-ui.html

📂 패키지 구조
bash
코드 복사
com.andamiro.dashboard
 ┣ controller   # API 엔드포인트
 ┣ service      # 비즈니스 로직
 ┣ repository   # JPA Repository
 ┣ entity       # DB 매핑 엔티티
 ┣ dto          # Request/Response 객체
 ┣ config       # 설정 관련 코드
 ┗ security     # 인증/인가 관련 코드
📝 코드 컨벤션
네이밍 규칙
클래스명: PascalCase → UserService, IncidentController

메서드/변수명: camelCase → getIncidentById, incidentType

상수: UPPER_SNAKE_CASE → MAX_FILE_SIZE

패키지명: 소문자 → com.andamiro.dashboard.service

코드 스타일
들여쓰기: 4 spaces

한 줄 최대 길이: 120자

중괄호 {}는 줄바꿈 없이

java
코드 복사
if (condition) {
    // do something
}
불필요한 else 지양, 조기 return 권장

Stream, Optional 적극 활용

Lombok은 @Getter, @NoArgsConstructor, @Builder까지만 사용

예외 처리
IllegalArgumentException, IllegalStateException 기본 활용

도메인 예외는 커스텀 Exception 정의

Controller 단에서는 @RestControllerAdvice 활용

✅ 테스트 코드 규칙
테스트 메서드 네이밍

kotlin
코드 복사
fun `메서드명/상황/예상결과`() { ... }
예시:

kotlin
코드 복사
@Test
fun `registerIncident with invalid type throws exception`() { ... }
계층별 테스트

Unit Test: Service, Repository 중심

Integration Test: Controller + MockMvc

Given / When / Then 패턴 준수

반복되는 Given 절은 Fixture/Helper로 분리

복잡한 데이터 셋업은 JSON/SQL 파일 활용

🔐 Git Commit 규칙
형식: 타입: 설명

feat: 새로운 기능 추가

fix: 버그 수정

refactor: 리팩토링

test: 테스트 코드 추가/수정

docs: 문서 수정

chore: 빌드/환경설정/잡무

예시:

makefile
코드 복사
feat: 사고 등록 API 구현
fix: IncidentRepository NPE 버그 수정
🤝 협업 원칙
모든 PR은 최소 1인 이상 리뷰 후 머지

PR 단위는 작은 단위로 유지 (한 PR = 하나의 기능)

코드 리뷰 시 주요 관심사와 책임 분리에 집중

테스트 코드를 통해 피드백 받고 리팩토링 적극 반영

👉 이 README는 프로젝트 초안 단계에서 작성된 것이며, 개발 진행 상황에 따라 지속적으로 업데이트됩니다.
