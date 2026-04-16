package com.rebac.engine.adapter.in.web.dto;

/**
 * 관계 확인 API 응답입니다.
 *
 * @param allowed 관계가 성립하면 {@code true}
 */
public record CheckResponseDto(boolean allowed) {}
