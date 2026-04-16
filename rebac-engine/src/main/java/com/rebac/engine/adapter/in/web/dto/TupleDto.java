package com.rebac.engine.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * REST 요청·응답에서 사용하는 단일 관계 튜플 표현입니다.
 *
 * @param user     주체 OpenFGA 문자열
 * @param relation 관계 이름
 * @param object   객체 OpenFGA 문자열
 */
public record TupleDto(
        @NotBlank String user,
        @NotBlank String relation,
        @NotBlank String object) {}
