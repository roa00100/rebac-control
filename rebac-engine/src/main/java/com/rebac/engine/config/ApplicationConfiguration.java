package com.rebac.engine.config;

import com.rebac.core.service.RebacService;
import com.rebac.core.spi.RebacEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 레벨 Bean 정의입니다. 코어 {@link RebacService}를 SPI {@link RebacEngine}에 연결합니다.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * @param engine OpenFGA 구현 등 런타임에 주입되는 {@link RebacEngine}
     * @return 웹 계층에서 사용할 서비스
     */
    @Bean
    RebacService rebacService(RebacEngine engine) {
        return new RebacService(engine);
    }
}
