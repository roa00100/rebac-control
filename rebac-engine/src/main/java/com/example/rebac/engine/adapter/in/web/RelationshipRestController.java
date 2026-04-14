package com.example.rebac.engine.adapter.in.web;

import com.example.rebac.core.domain.RelationshipCheckQuery;
import com.example.rebac.core.domain.RelationshipTuple;
import com.example.rebac.core.service.RebacService;
import com.example.rebac.engine.adapter.in.web.dto.CheckRequestDto;
import com.example.rebac.engine.adapter.in.web.dto.CheckResponseDto;
import com.example.rebac.engine.adapter.in.web.dto.TupleBatchRequest;
import com.example.rebac.engine.adapter.in.web.dto.TupleDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReBAC 튜플 적재·조회 및 관계 확인을 노출하는 REST 어댑터입니다.
 *
 * <p>도메인 로직은 {@link RebacService}에 위임하며, 경로는 {@code /api/v1} 아래에 정리됩니다.
 */
@RestController
@RequestMapping("/api/v1")
public class RelationshipRestController {

    private final RebacService rebacService;

    /**
     * @param rebacService 코어 유스케이스 서비스
     */
    public RelationshipRestController(RebacService rebacService) {
        this.rebacService = rebacService;
    }

    /**
     * 런타임 메타데이터(엔진 이름·간단 설명)를 반환합니다.
     *
     * @return 엔진 식별 정보
     */
    @GetMapping("/meta")
    public EngineMeta meta() {
        return new EngineMeta(
                "rebac-engine",
                "REST → rebac-core(RebacService) → RebacEngine SPI. OpenFGA 구현은 rebac-openfga 모듈 자동구성.");
    }

    /**
     * 배치로 관계 튜플을 적재합니다. {@code mode}가 비어 있으면 {@code append}로 처리합니다.
     *
     * @param body 모드 및 튜플 목록
     */
    @PostMapping("/tuples")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ingestTuples(@Valid @RequestBody TupleBatchRequest body) {
        String mode = body.mode() == null ? "append" : body.mode();
        List<RelationshipTuple> tuples =
                body.tuples().stream().map(RelationshipRestController::toDomain).toList();
        rebacService.ingestTuples(mode, tuples);
    }

    /**
     * 단일 관계에 대한 허용 여부를 확인합니다.
     *
     * @param body 사용자·관계·객체
     * @return 허용 결과
     */
    @PostMapping("/check")
    public CheckResponseDto check(@Valid @RequestBody CheckRequestDto body) {
        var q = new RelationshipCheckQuery(body.user(), body.relation(), body.object());
        return new CheckResponseDto(rebacService.check(q));
    }

    /**
     * 현재 스토어에 저장된 튜플을 모두 조회합니다.
     *
     * @return 튜플 DTO 목록
     */
    @GetMapping("/tuples")
    public List<TupleDto> listTuples() {
        return rebacService.listTuples().stream()
                .map(t -> new TupleDto(t.user(), t.relation(), t.object()))
                .toList();
    }

    private static RelationshipTuple toDomain(TupleDto d) {
        return new RelationshipTuple(d.user(), d.relation(), d.object());
    }

    /**
     * {@link #meta()} 응답 본문입니다.
     *
     * @param name        엔진 또는 애플리케이션 식별자
     * @param description 아키텍처·배포에 대한 짧은 설명
     */
    public record EngineMeta(String name, String description) {}
}
