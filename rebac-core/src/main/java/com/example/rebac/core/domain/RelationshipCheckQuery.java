package com.example.rebac.core.domain;

import java.util.Objects;

/**
 * 단일 관계에 대한 권한(또는 관계 존재) 확인 요청입니다.
 * {@link com.example.rebac.core.spi.RebacEngine#check(RelationshipCheckQuery)}에 전달됩니다.
 *
 * @param user     주체
 * @param relation 확인할 관계
 * @param object   대상 객체
 */
public record RelationshipCheckQuery(String user, String relation, String object) {

    public RelationshipCheckQuery {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(relation, "relation");
        Objects.requireNonNull(object, "object");
        if (user.isBlank() || relation.isBlank() || object.isBlank()) {
            throw new IllegalArgumentException("user, relation, object는 비어 있을 수 없습니다.");
        }
    }
}
