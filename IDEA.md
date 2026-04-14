---

# ReBAC 완벽 정리 및 Spring Boot 구현 가이드

## 1. 핵심 질문: "ReBAC 엔진 중에 Java로 된 것도 있나요?"

결론부터 말씀드리면, **시장 구도를 주도하는 독립적인 범용 ReBAC '엔진(서버)' 자체는 대부분 Go(Golang) 언어로 작성되어 있습니다.** * **이유:** Google Zanzibar 모델은 초당 수만 건의 그래프 탐색(권한 추론)을 지연 없이 처리해야 하므로, 동시성 처리와 메모리 관리에 유리한 Go 언어가 업계 표준으로 자리 잡았습니다. (예: OpenFGA, SpiceDB, Ory Keto 모두 Go 언어 기반입니다.)

**하지만 Spring Boot(Java) 생태계에서 이를 완벽하게 구현하고 연동하는 데는 전혀 문제가 없습니다.** 실무에서는 다음과 같은 3가지 선택지가 있습니다.

1.  **[가장 추천] 독립 엔진 + Java SDK 연동:** OpenFGA나 SpiceDB 서버를 별도 컨테이너로 띄우고, Spring Boot에서는 공식 Java SDK를 통해 API 질의만 던지는 마이크로서비스 형태입니다.
2.  **경량 라이브러리 사용 (jCasbin):** 외부 서버를 두기 싫다면, 권한 인가 라이브러리인 Casbin의 Java 버전(`jCasbin`)을 Spring Boot 내부에 탑재할 수 있습니다. jCasbin은 제한적인 형태의 ReBAC 모델을 지원합니다.
3.  **직접 구현 (Neo4j + Java):** 메인 데이터베이스 외에 Neo4j 같은 그래프 DB를 구축하고, Spring Data Neo4j를 이용해 Java로 권한 추론 로직을 직접 작성합니다.

---

## 2. Spring Boot 실전 구현 아키텍처 (OpenFGA / SpiceDB 기준)

SaaS 환경에서 가장 권장되는 1번 방식(외부 엔진 + Java SDK)을 기준으로 Spring Boot 시스템을 어떻게 구성하는지 단계별로 설명합니다.

### Step 1: 개념 매핑 (비즈니스 로직 vs 인가 로직의 분리)
Spring Boot 애플리케이션 안에는 **"누가 누구인가?"(인증)**와 **"이 작업을 어떻게 처리할 것인가?"(비즈니스 로직)**만 남깁니다. **"이 사람이 이 기능을 쓸 수 있는가?"(인가)**는 모두 외부 ReBAC 엔진으로 넘깁니다.

### Step 2: 관계 튜플(Tuple)의 작성 및 동기화
사용자가 조직에 초대되거나, 요금제를 결제할 때 Spring Boot 서비스는 DB에 데이터를 저장함과 동시에 ReBAC 엔진에 **관계(Tuple)**를 기록(Write)해야 합니다.

* **비즈니스 이벤트:** "Alice가 AcmeCorp의 Admin으로 임명됨"
* **엔진으로 전송할 데이터 (Tuple):** `user:alice`는 `organization:acmcorp`의 `admin`이다.

### Step 3: Spring Boot 실제 구현 코드 (AOP 기반)

API 요청이 들어왔을 때, 비즈니스 로직이 실행되기 전 AOP(Aspect-Oriented Programming)를 이용해 권한을 검증하는 것이 가장 깔끔합니다.

#### 1) 의존성 추가 (예: OpenFGA Java SDK)
```gradle
// build.gradle
implementation 'dev.openfga:openfga-sdk:0.4.0'
```

#### 2) 커스텀 어노테이션 생성
개발자들이 컨트롤러에 쉽게 붙일 수 있는 어노테이션을 만듭니다.
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RebacCheck {
    String relation();      // 예: "can_invoke" (호출 가능 여부)
    String objectType();    // 예: "api_feature"
    String objectId();      // 예: "advanced_reports"
}
```

#### 3) AOP Aspect 구현 (권한 가로채기)
이 부분이 Spring Boot와 ReBAC 엔진이 만나는 핵심 지점입니다.

```java
@Aspect
@Component
@RequiredArgsConstructor
public class RebacAuthorizationAspect {

    private final OpenFgaClient fgaClient; // SDK 클라이언트

    @Around("@annotation(rebacCheck)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RebacCheck rebacCheck) throws Throwable {
        
        // 1. 현재 로그인한 사용자 정보 추출 (Spring Security Context 등)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String userEntity = "user:" + userId;
        
        // 2. 확인할 객체 구성
        String objectEntity = rebacCheck.objectType() + ":" + rebacCheck.objectId();
        
        // 3. ReBAC 엔진(OpenFGA)에 질의 (Check API)
        // "이 유저가 이 객체에 대해 해당 관계(권한)를 가지는가?"
        ClientCheckRequest request = new ClientCheckRequest()
                .user(userEntity)
                .relation(rebacCheck.relation())
                .object(objectEntity);

        boolean isAllowed = fgaClient.check(request).get().isAllowed();

        if (!isAllowed) {
            throw new AccessDeniedException("해당 API 기능에 대한 접근 권한이 없습니다.");
        }

        // 권한이 있으면 원래의 비즈니스 로직 실행
        return joinPoint.proceed();
    }
}
```

#### 4) 컨트롤러에 적용
이제 API 구현 시, 비즈니스 로직 내부에 `if (user.getRole() == ... )` 같은 지저분한 코드가 완전히 사라집니다.

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @RebacCheck(relation = "can_invoke", objectType = "api_feature", objectId = "advanced_reports")
    @PostMapping("/advanced")
    public ResponseEntity<String> generateAdvancedReport() {
        // 권한 검증은 이미 AOP에서 외부 엔진을 통해 완료됨
        return ResponseEntity.ok("고급 리포트 생성 완료");
    }
}
```

---

## 3. 실무 도입 시 직면하게 될 과제 (주의점)

단순히 API를 찌르는 것은 쉽지만, SaaS 환경에서 ReBAC 도입 시 다음과 같은 고민이 필요합니다.

1.  **모델링의 난해함:** 처음 스키마(사용자, 역할, 플랜, API 기능 간의 관계)를 설계하는 것이 생각보다 어렵습니다. 종이에 그래프를 그려가며 "A관계가 B관계를 포함한다"는 식의 계층 설계를 탄탄히 해야 합니다.
2.  **데이터 동기화 (Dual Write 문제):** Spring Boot의 메인 DB (예: MySQL)에 회원 상태가 바뀌었을 때, ReBAC 엔진에도 튜플을 업데이트해야 합니다. 이때 MySQL은 성공했는데 ReBAC 엔진 업데이트는 실패하면 권한 불일치가 발생합니다. 이를 막기 위해 실무에서는 이벤트 브로커(Kafka)나 CDC(Change Data Capture) 패턴을 도입하기도 합니다.
3.  **지연 시간 (Latency):** 모든 API 요청마다 외부 인가 엔진을 찔러야 하므로 네트워크 지연이 발생할 수 있습니다. 따라서 ReBAC 엔진은 Spring Boot 서버와 물리적으로 가장 가까운 네트워크(같은 VPC 내부 등)에 배포해야 합니다.

---

문서로 정리해 보니 시스템의 큰 그림이 조금 그려지시는지요? 외부 엔진(OpenFGA나 SpiceDB)을 도커(Docker)로 로컬에 띄워서 테스트해 보는 과정이 필요한데, 혹시 이 두 가지 엔진 중 어떤 것에 맞춰 초기 모델링 예시를 더 자세히 보고 싶으신가요?