package com.rebac.openfga;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenFGA 연동 설정입니다. {@code openfga.*} 프로퍼티로 바인딩됩니다.
 *
 * @param apiUrl                         OpenFGA API 베이스 URL(예: {@code http://localhost:8080})
 * @param storeName                      사용할 스토어 이름(없으면 자동 생성)
 * @param writeDefaultAuthorizationModel {@code false}이면 스토어에 모델이 없어도 기본 document 모델을
 *                                       쓰지 않음(외부에서 먼저 모델을 적재하는 경우)
 */
@ConfigurationProperties(prefix = "openfga")
public record OpenFgaProperties(String apiUrl, String storeName, Boolean writeDefaultAuthorizationModel) {

    public OpenFgaProperties {
        if (writeDefaultAuthorizationModel == null) {
            writeDefaultAuthorizationModel = true;
        }
    }
}
