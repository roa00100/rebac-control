# rebac-control

Zanzibar 스타일 **ReBAC(Relationship-Based Access Control)** 튜플을 REST로 받아 저장·조회·검증하는 **제어 엔진** 샘플입니다. 백엔드 권한 평가는 [OpenFGA](https://openfga.dev/)에 위임하고, 애플리케이션 코어는 **SPI**만 의존합니다.

## 구성

| 모듈 | 역할 |
|------|------|
| **rebac-core** | 도메인(`RelationshipTuple`, `RelationshipCheckQuery`), `RebacEngine` SPI, `RebacService` |
| **rebac-openfga** | `RebacEngine`의 OpenFGA 구현, Spring Boot 자동구성(스토어·데모 인가 모델 부트스트랩) |
| **rebac-engine** | Spring Boot 3 앱, REST 어댑터(`RelationshipRestController`) |

- **Java 21**, **Spring Boot 3.4**, OpenFGA Java SDK **0.9.7**

## 빠른 시작 (Docker)

저장소 루트에서:

```bash
docker compose up --build
```

- **rebac-engine** REST: `http://localhost:21001`
- **OpenFGA** API: `http://localhost:21010`
- Postgres(OpenFGA 전용): `localhost:21020`

컨테이너 환경에서는 `OPENFGA_API_URL=http://openfga:8080`으로 엔진이 OpenFGA에 붙습니다.

## 로컬 실행 (OpenFGA는 직접 띄운 경우)

1. OpenFGA가 `application.yml`의 `openfga.api-url`(기본 `http://127.0.0.1:21010`)에서 동작하도록 준비합니다.
2. 루트에서:

```bash
./gradlew :rebac-engine:bootRun
```

## 설정

`rebac-engine/src/main/resources/application.yml` 및 환경 변수:

| 키 / 환경 변수 | 설명 |
|----------------|------|
| `SERVER_PORT` | HTTP 포트 (기본 `21001`) |
| `OPENFGA_API_URL` | OpenFGA API 베이스 URL |
| `OPENFGA_STORE_NAME` | 사용할 스토어 이름 (없으면 자동 생성) |
| `openfga.enabled` | `false`면 OpenFGA 자동구성 비활성 + no-op 엔진(테스트용) |

## REST API (`/api/v1`)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/meta` | 엔진 메타 정보 |
| `POST` | `/tuples` | 튜플 일괄 적재 (`mode`: `replace` \| `append`) |
| `POST` | `/check` | 관계 허용 여부 확인 |
| `GET` | `/tuples` | 저장된 튜플 목록 |

요청/응답 DTO는 `rebac-engine`의 `adapter.in.web.dto` 패키지를 참고하세요.

## 빌드·테스트

```bash
./gradlew build
```

테스트 프로파일에서는 `openfga.enabled: false`로 외부 OpenFGA 없이 `@WebMvcTest`가 동작합니다.

## 라이선스

예제 프로젝트입니다. 필요 시 저장소에 `LICENSE`를 추가하세요.
