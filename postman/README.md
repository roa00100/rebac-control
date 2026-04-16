# Postman (테스트용 API)

## 가져오기

1. Postman → **Import** → `rebac-control.postman_collection.json`
2. **Import** → `rebac-control.postman_environment.json` → 환경 이름 선택 후 **Select** 활성화

## 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `rebac_engine_base` | `http://localhost:21001` | rebac-engine |

Docker 포트를 바꿨다면 환경에서 위 값만 수정하면 됩니다.

## 권장 순서

1. 저장소 루트에서 `docker compose up --build` 등으로 **rebac-engine**과 OpenFGA 스택을 기동합니다.
2. 컬렉션 폴더 **rebac-engine** 아래 요청으로 `meta`, `tuples`, `check` 를 호출합니다.

## 참고

- **Ingest tuples (replace)** 는 OpenFGA 스토어 튜플을 전부 갈아엎습니다. 공용 DB에서는 사용 전에 확인하세요.
- **Ingest tuples (replace, empty = clear)** 는 튜플만 비우고 새로 쓰지 않습니다.
