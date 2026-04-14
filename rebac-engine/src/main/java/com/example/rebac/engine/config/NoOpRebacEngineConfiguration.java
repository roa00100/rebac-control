package com.example.rebac.engine.config;

import com.example.rebac.core.domain.RelationshipCheckQuery;
import com.example.rebac.core.domain.RelationshipTuple;
import com.example.rebac.core.spi.RebacEngine;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code openfga.enabled=false}일 때만 활성화됩니다. OpenFGA 자동구성과 상호 배타이며,
 * 테스트나 오프라인 실행 시 외부 의존 없이 컨텍스트를 기동하기 위해 사용합니다.
 */
@Configuration
@ConditionalOnProperty(name = "openfga.enabled", havingValue = "false")
public class NoOpRebacEngineConfiguration {

    /**
     * 적재는 무시하고, 체크는 항상 {@code false}, 목록은 빈 배열을 반환하는 스텁 엔진입니다.
     *
     * @return no-op {@link RebacEngine}
     */
    @Bean
    RebacEngine noopRebacEngine() {
        return new RebacEngine() {
            @Override
            public void ingestTuples(String mode, List<RelationshipTuple> tuples) {
                // no-op
            }

            @Override
            public boolean check(RelationshipCheckQuery query) {
                return false;
            }

            @Override
            public List<RelationshipTuple> listAllTuples() {
                return List.of();
            }
        };
    }
}
