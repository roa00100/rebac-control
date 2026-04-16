package com.rebac.openfga;

import com.rebac.core.domain.RelationshipCheckQuery;
import com.rebac.core.domain.RelationshipTuple;
import com.rebac.core.spi.RebacEngine;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.configuration.ClientDeleteTuplesOptions;
import dev.openfga.sdk.api.configuration.ClientReadOptions;
import dev.openfga.sdk.api.configuration.ClientWriteTuplesOptions;
import dev.openfga.sdk.api.model.Tuple;
import dev.openfga.sdk.api.model.WriteRequestDeletes;
import dev.openfga.sdk.api.model.WriteRequestWrites;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * OpenFGA HTTP API를 사용하는 {@link RebacEngine} 구현입니다.
 *
 * <p>스토어·모델은 {@link com.rebac.openfga.autoconfigure.OpenFgaEngineAutoConfiguration}에서
 * 부트스트랩되며, 이 클래스는 이미 설정된 {@link OpenFgaClient}로 읽기·쓰기·체크만 수행합니다.
 */
public final class OpenFgaRebacEngine implements RebacEngine {

    /** OpenFGA Read API 상한(현재 스펙: 1~100). */
    private static final int READ_PAGE_SIZE = 100;

    private final OpenFgaClient client;

    /**
     * @param client 스토어 ID가 설정된 OpenFGA 클라이언트
     */
    public OpenFgaRebacEngine(OpenFgaClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     *
     * <p>중복 튜플은 무시하고, {@code replace} 모드에서는 전체 삭제 후 쓰기를 수행합니다.
     */
    @Override
    public void ingestTuples(String mode, List<RelationshipTuple> tuples) {
        var m = mode == null ? "append" : mode.toLowerCase(Locale.ROOT);
        if ("replace".equals(m)) {
            deleteAllTuples();
        } else if (!"append".equals(m)) {
            throw new IllegalArgumentException("mode는 replace 또는 append 만 허용됩니다.");
        }
        if (tuples.isEmpty()) {
            return;
        }
        var keys = tuples.stream().map(OpenFgaRebacEngine::toWriteKey).toList();
        var options = new ClientWriteTuplesOptions()
                .onDuplicate(WriteRequestWrites.OnDuplicateEnum.IGNORE);
        join(() -> client.writeTuples(keys, options));
    }

    /** {@inheritDoc} */
    @Override
    public boolean check(RelationshipCheckQuery query) {
        var req = new ClientCheckRequest()
                .user(query.user())
                .relation(query.relation())
                ._object(query.object());
        return Boolean.TRUE.equals(join(() -> client.check(req)).getAllowed());
    }

    /**
     * {@inheritDoc}
     *
     * <p>페이지 단위로 읽어 continuation 토큰이 없을 때까지 이어 붙입니다.
     */
    @Override
    public List<RelationshipTuple> listAllTuples() {
        var out = new ArrayList<RelationshipTuple>();
        String continuation = null;
        do {
            var opts = new ClientReadOptions().pageSize(READ_PAGE_SIZE);
            if (continuation != null && !continuation.isBlank()) {
                opts.continuationToken(continuation);
            }
            ClientReadResponse resp = join(() -> client.read(new ClientReadRequest(), opts));
            List<Tuple> pageTuples = resp.getTuples();
            if (pageTuples != null) {
                for (Tuple t : pageTuples) {
                    var k = t.getKey();
                    out.add(new RelationshipTuple(k.getUser(), k.getRelation(), k.getObject()));
                }
            }
            continuation = resp.getContinuationToken();
        } while (continuation != null && !continuation.isBlank());
        return List.copyOf(out);
    }

    private void deleteAllTuples() {
        List<RelationshipTuple> existing = listAllTuples();
        if (existing.isEmpty()) {
            return;
        }
        var deletes = existing.stream().map(OpenFgaRebacEngine::toDeleteKey).toList();
        var options = new ClientDeleteTuplesOptions()
                .onMissing(WriteRequestDeletes.OnMissingEnum.IGNORE);
        join(() -> client.deleteTuples(deletes, options));
    }

    private static ClientTupleKey toWriteKey(RelationshipTuple t) {
        return new ClientTupleKey().user(t.user()).relation(t.relation())._object(t.object());
    }

    private static ClientTupleKeyWithoutCondition toDeleteKey(RelationshipTuple t) {
        return new ClientTupleKeyWithoutCondition()
                .user(t.user())
                .relation(t.relation())
                ._object(t.object());
    }

    @FunctionalInterface
    private interface AsyncCall<T> {
        CompletableFuture<T> run() throws Exception;
    }

    private static <T> T join(AsyncCall<T> call) {
        CompletableFuture<T> future;
        try {
            future = call.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            var c = e.getCause();
            if (c instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(c != null ? c : e);
        }
    }
}
