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
```

## 애플리케이션 실행
./gradlew bootRun
👉 http://15.164.99.177:80/

---
## 📂 패키지 구조
```bash
com.andamiro.dashboard
 ┣ controller   # API 엔드포인트
 ┣ service      # 비즈니스 로직
 ┣ repository   # JPA Repository
 ┣ entity       # DB 매핑 엔티티
 ┣ dto          # Request/Response 객체
 ┣ config       # 설정 관련 코드
 ┗ security     # 인증/인가 관련 코드
```
---

## 📝 코드 컨벤션
# 네이밍 규칙
- 클래스명: PascalCase → UserService, IncidentController
- 메서드/변수명: camelCase → getIncidentById, incidentType
- Lombok은 @Getter, @NoArgsConstructor, @Builder까지만 사용

# 예외 처리
- IllegalArgumentException, IllegalStateException 기본 활용
- 도메인 예외는 커스텀 Exception 정의

---

## 🔐 Git Commit 규칙
형식: 타입: 설명

- feat: 새로운 기능 추가
- fix: 버그 수정
- refactor: 리팩토링
- test: 테스트 코드 추가/수정
- docs: 문서 수정
- chore: 빌드/환경설정/잡무

ex)
feat: 사고 등록 API 구현
fix: IncidentRepository NPE 버그 수정

---

👉 이 README는 프로젝트 초안 단계에서 작성된 것이며, 개발 진행 상황에 따라 지속적으로 업데이트됩니다.
