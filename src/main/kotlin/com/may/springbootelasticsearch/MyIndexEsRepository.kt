package com.may.springbootelasticsearch

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Repository

@Repository
class MyIndexEsRepository(
    private val elasticsearchClient: RestHighLevelClient
) {
    fun getMessageByTerm(term: String): SearchResponse {
        val searchSourceBuilder = SearchSourceBuilder()
        val filter = QueryBuilders.boolQuery()
            .filter(
                QueryBuilders.termQuery(
                    "message",
                    term
                )
            )
        searchSourceBuilder.query(filter)

        val searchRequest = SearchRequest().indices("my_index").source(searchSourceBuilder)
        return elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
    }
}