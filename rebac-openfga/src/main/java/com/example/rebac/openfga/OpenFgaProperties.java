package com.example.rebac.openfga;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenFGA 연동 설정입니다. {@code openfga.*} 프로퍼티로 바인딩됩니다.
 *
 * @param apiUrl    OpenFGA API 베이스 URL(예: {@code http://localhost:8080})
 * @param storeName 사용할 스토어 이름(없으면 자동 생성)
 */
@ConfigurationProperties(prefix = "openfga")
public record OpenFgaProperties(String apiUrl, String storeName) {}
