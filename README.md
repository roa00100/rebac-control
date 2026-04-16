# rebac-control

Zanzibar 스타일 **ReBAC(Relationship-Based Access Control)** 튜플을 REST로 받아 저장·조회·검증하는 **제어 엔진** 샘플입니다. 백엔드 권한 평가는 [OpenFGA](https://openfga.dev/)에 위임하고, 애플리케이션 코어는 **SPI**만 의존합니다.

## 구성

| 모듈 | 역할 |
|------|------|
| **rebac-core** | 도메인(`RelationshipTuple`, `RelationshipCheckQuery`), `RebacEngine` SPI, `RebacService` |
| **rebac-openfga-adapter** | `RebacEngine`용 OpenFGA 어댑터: 구현·Spring Boot 자동구성(스토어·기본 인가 모델) |
| **rebac-engine** | Spring Boot 3 앱, REST 어댑터(`RelationshipRestController`) |

- **Java 21**, **Spring Boot 3.4**, OpenFGA Java SDK **0.9.7**

엔진 확장 기능·진행 단계는 [`docs/ENGINE-ROADMAP.md`](docs/ENGINE-ROADMAP.md)를 참고하세요. AI/에이전트·기여 공통 지침은 [`.cursor/rules/rebac-engine-charter.mdc`](.cursor/rules/rebac-engine-charter.mdc)(항상 적용) 및 [`AGENTS.md`](AGENTS.md)를 따릅니다.

## 빠른 시작 (Docker)

저장소 루트에서:

```bash
docker compose up --build
```

`rebac-engine/Dockerfile` 은 **별도 테스트 모듈이 아니라** Gradle `test` 태스크(`rebac-engine` 등 각 모듈의 `src/test`)를 이미지 빌드 중에 실행합니다. 런타임 컨테이너에는 테스트 코드가 포함되지 않습니다. 빌드 컨텍스트는 멀티모듈 Gradle을 위해 저장소 루트(`.`)입니다.

- **rebac-engine** REST: `http://localhost:21001`
- **OpenFGA** HTTP API: `http://localhost:21010`, gRPC: `localhost:21011`
- **MySQL**(OpenFGA 전용): `localhost:21020` (기본 루트 비밀번호는 compose 기본값·또는 루트 `.env`의 `MYSQL_ROOT_PASSWORD`; DB 이름은 `docker-compose.yml` 참고)

`docker-compose.yml`의 OpenFGA 구간은 [공식 Docker 가이드(MySQL)](https://openfga.dev/docs/getting-started/setup-openfga/docker#using-mysql)와 같은 흐름입니다(MySQL 기동 → `migrate` → `openfga` 실행). `OPENFGA_DATASTORE_MAX_OPEN_CONNS`는 MySQL `max_connections`(200)보다 작게 두었습니다. Playground는 끈 채(`OPENFGA_PLAYGROUND_ENABLED=false`) 포트만 210xx로 매핑했습니다.

컨테이너 네트워크 안에서는 `OPENFGA_API_URL=http://openfga:8080`으로 엔진이 OpenFGA HTTP에 붙습니다.

### 재시작과 데이터

- **`migrate`는 스키마용**입니다. DB가 이미 마이그레이션된 상태면 다시 실행돼도 일반적으로 튜플 같은 **업무 데이터를 비우지 않습니다**(버전에 맞는 스키마만 적용·유지).
- **데이터가 사라질 수 있는 경우**는 주로 **볼륨을 지울 때**입니다. MySQL 데이터는 이름 붙은 볼륨 `openfga-mysql-data`에 있으므로, `docker compose down`만으로는 유지되고, **`docker compose down -v`** 또는 `docker volume rm …`처럼 **볼륨까지 제거**하면 초기화됩니다.
- **운영 수준의 안정성**(백업, 복제, 장애 조치)은 이 compose가 목표로 하지 않습니다. 로컬·데모용이며, 상용은 [OpenFGA 운영 가이드](https://openfga.dev/docs/getting-started/running-in-production)와 DB 백업 전략을 따르는 것이 맞습니다.

## 로컬 실행 (OpenFGA는 직접 띄운 경우)

1. OpenFGA가 `application.yml`의 `openfga.api-url`(기본 `http://127.0.0.1:21010`)에서 동작하도록 준비합니다.
2. 루트에서(Windows는 `gradlew.bat`):

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

## Postman

테스트용 컬렉션·환경 파일은 [`postman/`](postman/README.md)에 있습니다. Import 후 `rebac_engine_base` 만 맞추면 엔진 API를 호출할 수 있습니다.

## 빌드·테스트

```bash
./gradlew build
```

테스트 프로파일에서는 `openfga.enabled: false`로 외부 OpenFGA 없이 `@WebMvcTest`가 동작합니다.

## 라이선스

예제 프로젝트입니다. 필요 시 저장소에 `LICENSE`를 추가하세요.
