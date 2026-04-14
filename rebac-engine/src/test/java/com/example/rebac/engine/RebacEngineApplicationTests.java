package com.example.rebac.engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.rebac.core.domain.RelationshipTuple;
import com.example.rebac.core.service.RebacService;
import com.example.rebac.engine.adapter.in.web.RelationshipRestController;
import com.example.rebac.engine.adapter.in.web.RestExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RelationshipRestController.class)
@Import(RestExceptionHandler.class)
class RebacEngineApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RebacService rebacService;

    @Test
    void metaOk() throws Exception {
        mockMvc.perform(get("/api/v1/meta")).andExpect(status().isOk());
    }

    @Test
    void delegatesIngestAndCheckToService() throws Exception {
        doNothing().when(rebacService).ingestTuples(anyString(), anyList());
        when(rebacService.check(any())).thenReturn(true);
        when(rebacService.listTuples())
                .thenReturn(List.of(new RelationshipTuple("user:alice", "owner", "document:doc1")));

        mockMvc.perform(post("/api/v1/tuples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"mode":"replace","tuples":[
                                  {"user":"user:alice","relation":"owner","object":"document:doc1"}
                                ]}
                                """))
                .andExpect(status().isNoContent());

        verify(rebacService).ingestTuples(eq("replace"), anyList());

        mockMvc.perform(post("/api/v1/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"user":"user:alice","relation":"viewer","object":"document:doc1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));

        mockMvc.perform(get("/api/v1/tuples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user").value("user:alice"));
    }
}
