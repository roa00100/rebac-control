package com.example.rebac.core.service;

import com.example.rebac.core.domain.RelationshipCheckQuery;
import com.example.rebac.core.domain.RelationshipTuple;
import com.example.rebac.core.spi.RebacEngine;
import java.util.List;

/**
 * ReBAC 유스케이스를 수행하는 애플리케이션 서비스입니다. Spring 등 프레임워크에 의존하지 않으며,
 * 주입된 {@link RebacEngine} SPI에 위임합니다.
 */
public class RebacService {

    private final RebacEngine engine;

    /**
     * @param engine 실제 저장·평가를 담당하는 SPI 구현
     */
    public RebacService(RebacEngine engine) {
        this.engine = engine;
    }

    /**
     * @param mode   {@link RebacEngine#ingestTuples(String, List)}와 동일
     * @param tuples 적재할 튜플
     */
    public void ingestTuples(String mode, List<RelationshipTuple> tuples) {
        engine.ingestTuples(mode, tuples);
    }

    /**
     * @param query 확인 요청
     * @return 허용 여부
     */
    public boolean check(RelationshipCheckQuery query) {
        return engine.check(query);
    }

    /**
     * @return 현재 스토어의 튜플 스냅샷
     */
    public List<RelationshipTuple> listTuples() {
        return engine.listAllTuples();
    }
}
