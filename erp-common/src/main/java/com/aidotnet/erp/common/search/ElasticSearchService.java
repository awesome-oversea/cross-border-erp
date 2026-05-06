package com.aidotnet.erp.common.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchService {

    private final ElasticsearchClient esClient;

    public ElasticSearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public <T> void indexDocument(String index, String id, T document) throws IOException {
        esClient.index(IndexRequest.of(b -> b
                .index(index)
                .id(id)
                .document(document)));
    }

    public <T> List<T> search(String index, Class<T> clazz, Map<String, String> mustMatches) throws IOException {
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder().index(index);
        if (mustMatches != null && !mustMatches.isEmpty()) {
            searchBuilder.query(q -> q.bool(b -> {
                mustMatches.forEach((field, value) -> b.must(m -> m.match(mt -> mt.field(field).query(value))));
                return b;
            }));
        }
        SearchResponse<T> response = esClient.search(searchBuilder.build(), clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }

    public void deleteDocument(String index, String id) throws IOException {
        esClient.delete(d -> d.index(index).id(id));
    }
}
