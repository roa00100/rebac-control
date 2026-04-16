package com.rebac.core.spi;

import com.rebac.core.domain.RelationshipCheckQuery;
import com.rebac.core.domain.RelationshipTuple;
import java.util.List;

/**
 * ReBAC 관계 저장 및 평가를 위한 서비스 제공자 인터페이스(SPI)입니다.
 *
 * <p>애플리케이션 코어는 이 타입만 의존하고, OpenFGA·SpiceDB·인메모리 등 구체 구현은 별 Gradle 모듈에서
 * 제공합니다.
 */
public interface RebacEngine {

    /**
     * 관계 튜플을 스토어에 반영합니다.
     *
     * @param mode   {@code replace}이면 스토어의 기존 튜플을 비운 뒤 적재, {@code append}이면 기존에 병합
     * @param tuples 적재할 튜플 목록(빈 목록은 구현체에 따라 무시되거나 전체 삭제의 의미가 될 수 있음)
     */
    void ingestTuples(String mode, List<RelationshipTuple> tuples);

    /**
     * 주어진 주체가 객체에 대해 관계를 갖는지(또는 모델에 따라 암시되는지) 확인합니다.
     *
     * @param query 사용자·관계·객체
     * @return 관계가 성립하면 {@code true}
     */
    boolean check(RelationshipCheckQuery query);

    /**
     * 현재 스토어에 저장된 튜플을 조회합니다. 구현체는 페이지 단위 API를 내부에서 묶어 반환할 수 있습니다.
     *
     * @return 튜플 목록(없으면 빈 목록)
     */
    List<RelationshipTuple> listAllTuples();
}
