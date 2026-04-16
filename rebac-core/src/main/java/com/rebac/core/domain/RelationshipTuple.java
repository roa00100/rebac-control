package com.rebac.core.domain;

import java.util.Objects;

/**
 * Zanzibar 스타일의 관계 튜플을 표현합니다. 세 필드는 OpenFGA 등 엔진에 그대로 전달되는 문자열입니다.
 *
 * @param user     주체(예: {@code user:alice})
 * @param relation 관계 이름(예: {@code owner}, {@code viewer})
 * @param object   객체(예: {@code document:doc1})
 */
public record RelationshipTuple(String user, String relation, String object) {

    public RelationshipTuple {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(relation, "relation");
        Objects.requireNonNull(object, "object");
        if (user.isBlank() || relation.isBlank() || object.isBlank()) {
            throw new IllegalArgumentException("user, relation, object는 비어 있을 수 없습니다.");
        }
    }

    /**
     * 내부 구현에서 사용할 수 있는 결합 키입니다. 문자열에 파이프({@code |})가 포함되면 안 됩니다.
     *
     * @return {@code user|relation|object} 형태의 단일 키
     */
    public String tupleKey() {
        return user + "|" + relation + "|" + object;
    }
}
