package com.jpdictionary.dictionary.service.search.meilisearch;

import com.jpdictionary.config.MeilisearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MeilisearchClientTest {

    private MockRestServiceServer mockServer;
    private MeilisearchClient client;

    @BeforeEach
    void setUp() {
        MeilisearchProperties properties = new MeilisearchProperties();
        properties.setBaseUrl("http://localhost:7700");
        properties.setApiKey("test-key");
        properties.setIndexName("dictionary_entries");

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = MeilisearchClient.withMockBuilder(properties, builder);
    }

    @Test
    @DisplayName("Should create index if missing and configure Japanese/Vietnamese settings")
    void testEnsureIndexAndSettings() {
        mockServer.expect(requestTo("http://localhost:7700/indexes/dictionary_entries"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withResourceNotFound());

        mockServer.expect(requestTo("http://localhost:7700/indexes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("\"primaryKey\":\"id\"")))
                .andRespond(withSuccess("{\"taskUid\":1,\"status\":\"enqueued\"}", MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://localhost:7700/indexes/dictionary_entries/settings"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().string(containsString("\"jpn\"")))
                .andExpect(content().string(containsString("\"frequencyRank:asc\"")))
                .andExpect(content().string(containsString("\"disableOnAttributes\":[\"primaryForm\"]")))
                .andRespond(withSuccess("{\"taskUid\":2,\"status\":\"enqueued\"}", MediaType.APPLICATION_JSON));

        client.ensureIndexAndSettings();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should send PUT batch documents to Meilisearch")
    void testUpsertDocuments() {
        mockServer.expect(requestTo("http://localhost:7700/indexes/dictionary_entries/documents"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().string(containsString("\"primaryForm\":\"食べる\"")))
                .andRespond(withSuccess("{\"taskUid\":3,\"status\":\"enqueued\"}", MediaType.APPLICATION_JSON));

        MeilisearchSearchResponse.TaskResponse task = client.upsertDocuments(List.of(
                MeilisearchDocument.builder().id(1L).primaryForm("食べる").build()
        ));
        assertNotNull(task);
        assertEquals(3L, task.getTaskUid());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should send POST multi-search with indexUid populated")
    void testMultiSearch() {
        mockServer.expect(requestTo("http://localhost:7700/multi-search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("\"indexUid\":\"dictionary_entries\"")))
                .andExpect(content().string(containsString("\"q\":\"食べた\"")))
                .andExpect(content().string(containsString("\"q\":\"食べる\"")))
                .andRespond(withSuccess(
                        "{\"results\":[{\"hits\":[]},{\"hits\":[{\"id\":1,\"_rankingScore\":0.95}]}]}",
                        MediaType.APPLICATION_JSON
                ));

        MeilisearchSearchResponse.MultiSearchResponse response = client.multiSearch(List.of(
                MeilisearchSearchRequest.builder().q("食べた").limit(20).build(),
                MeilisearchSearchRequest.builder().q("食べる").limit(20).build()
        ));
        assertNotNull(response);
        assertEquals(2, response.getResults().size());
        assertEquals(1L, response.getResults().get(1).getHits().get(0).getId());
        mockServer.verify();
    }
}

