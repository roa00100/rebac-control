package com.rebac.engine.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 튜플 일괄 적재 요청 본문입니다. {@code mode}는 코어 {@code RebacEngine#ingestTuples}와 동일한 의미입니다.
 *
 * @param mode   {@code replace} 또는 {@code append}(또는 생략 시 append로 처리되는 경우 있음)
 * @param tuples 적재할 튜플 목록(비어 있어도 됨. {@code replace}이면 전체 삭제만 하고 끝낼 수 있음)
 */
public record TupleBatchRequest(String mode, @NotNull @Valid List<TupleDto> tuples) {}
