package com.example.rebac.engine.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 관계 확인 API 요청 본문입니다.
 *
 * @param user     주체
 * @param relation 확인할 관계
 * @param object   대상 객체
 */
public record CheckRequestDto(
        @NotBlank String user,
        @NotBlank String relation,
        @NotBlank String object) {}
