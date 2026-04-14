package com.example.rebac.engine.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 튜플 일괄 적재 요청 본문입니다. {@code mode}는 코어 {@code RebacEngine#ingestTuples}와 동일한 의미입니다.
 *
 * @param mode   {@code replace} 또는 {@code append}(또는 생략 시 append로 처리되는 경우 있음)
 * @param tuples 적재할 튜플 목록
 */
public record TupleBatchRequest(String mode, @NotEmpty @Valid List<TupleDto> tuples) {}
