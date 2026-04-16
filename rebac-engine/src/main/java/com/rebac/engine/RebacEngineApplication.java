package com.rebac.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ReBAC 제어 HTTP API를 제공하는 Spring Boot 진입점입니다.
 */
@SpringBootApplication
public class RebacEngineApplication {

    /**
     * @param args 표준 Spring Boot 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(RebacEngineApplication.class, args);
    }
}
