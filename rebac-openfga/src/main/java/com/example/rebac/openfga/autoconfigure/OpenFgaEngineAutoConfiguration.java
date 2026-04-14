package com.example.rebac.openfga.autoconfigure;

import com.example.rebac.core.spi.RebacEngine;
import com.example.rebac.openfga.OpenFgaProperties;
import com.example.rebac.openfga.OpenFgaRebacEngine;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientCreateStoreOptions;
import dev.openfga.sdk.api.configuration.ClientListStoresOptions;
import dev.openfga.sdk.api.configuration.ClientReadLatestAuthorizationModelOptions;
import dev.openfga.sdk.api.configuration.ClientWriteAuthorizationModelOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.Metadata;
import dev.openfga.sdk.api.model.ObjectRelation;
import dev.openfga.sdk.api.model.RelationMetadata;
import dev.openfga.sdk.api.model.RelationReference;
import dev.openfga.sdk.api.model.Store;
import dev.openfga.sdk.api.model.TypeDefinition;
import dev.openfga.sdk.api.model.Userset;
import dev.openfga.sdk.api.model.Usersets;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * OpenFGA SDK 클라이언트와 {@link RebacEngine} Bean을 등록하는 Spring Boot 자동구성입니다.
 *
 * <p>조건: 클래스패스에 OpenFGA SDK가 있고 {@code openfga.enabled=true}(기본값)일 때 활성화됩니다.
 * 스토어를 이름으로 찾거나 생성하고, 문서/소유자/뷰어 모델이 없으면 1.1 스키마로 작성합니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenFgaProperties.class)
@ConditionalOnClass(OpenFgaClient.class)
@ConditionalOnProperty(name = "openfga.enabled", havingValue = "true", matchIfMissing = true)
public class OpenFgaEngineAutoConfiguration {

    /**
     * 스토어를 해석한 뒤 클라이언트에 스토어 ID를 설정하고, 필요 시 인가 모델을 보장합니다.
     *
     * @param props API URL 및 스토어 이름
     * @return 스토어가 지정된 OpenFGA 클라이언트
     */
    @Bean
    @ConditionalOnMissingBean
    OpenFgaClient openFgaClient(OpenFgaProperties props) throws Exception {
        var baseConfig = new ClientConfiguration().apiUrl(props.apiUrl());
        var client = new OpenFgaClient(baseConfig);

        String storeId = resolveStoreId(client, props.storeName());
        client.setStoreId(storeId);

        ensureAuthorizationModel(client);

        return client;
    }

    /**
     * @param client {@link #openFgaClient(OpenFgaProperties)}에서 준비된 클라이언트
     * @return OpenFGA 백엔드에 연결된 {@link RebacEngine}
     */
    @Bean
    @ConditionalOnMissingBean(RebacEngine.class)
    RebacEngine rebacEngine(OpenFgaClient client) {
        return new OpenFgaRebacEngine(client);
    }

    /**
     * 기존 스토어 목록에서 이름이 일치하는 ID를 반환하고, 없으면 새 스토어를 만듭니다.
     */
    private static String resolveStoreId(OpenFgaClient client, String storeName) throws Exception {
        var stores = client.listStores(new ClientListStoresOptions()).get().getStores();
        var existing = (stores == null ? List.<Store>of() : stores).stream()
                .filter(s -> storeName.equals(s.getName()))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        return client.createStore(new CreateStoreRequest().name(storeName), new ClientCreateStoreOptions())
                .get()
                .getId();
    }

    /**
     * 최신 인가 모델이 있으면 그대로 두고, 없거나 조회 실패 시 데모용 document/user 모델을 등록합니다.
     */
    private static void ensureAuthorizationModel(OpenFgaClient client) throws Exception {
        try {
            var latest = client.readLatestAuthorizationModel(new ClientReadLatestAuthorizationModelOptions())
                    .get()
                    .getAuthorizationModel();
            if (latest != null && latest.getId() != null) {
                return;
            }
        } catch (Exception ignored) {
            // 스토어에 모델이 없음
        }
        var request = new WriteAuthorizationModelRequest()
                .schemaVersion("1.1")
                .typeDefinitions(List.of(
                        new TypeDefinition().type("user").relations(Map.of()),
                        new TypeDefinition()
                                .type("document")
                                .relations(Map.of(
                                        "owner",
                                        new Userset()._this(Map.of()),
                                        "viewer",
                                        new Userset().union(new Usersets()
                                                .child(List.of(
                                                        new Userset()._this(Map.of()),
                                                        new Userset()
                                                                .computedUserset(new ObjectRelation()
                                                                        .relation("owner")))))))
                                .metadata(new Metadata()
                                        .relations(Map.of(
                                                "owner",
                                                new RelationMetadata()
                                                        .directlyRelatedUserTypes(
                                                                List.of(new RelationReference().type("user"))),
                                                "viewer",
                                                new RelationMetadata()
                                                        .directlyRelatedUserTypes(
                                                                List.of(new RelationReference().type("user"))))))));
        client.writeAuthorizationModel(request, new ClientWriteAuthorizationModelOptions()).get();
    }
}
