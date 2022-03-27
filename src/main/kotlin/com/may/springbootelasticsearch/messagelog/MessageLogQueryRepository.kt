package com.may.springbootelasticsearch.messagelog

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Repository

@Repository
class MessageLogQueryRepository(
    private val elasticsearchClient: RestHighLevelClient,
) {
    fun getMessagesByTerm(term: String): SearchResponse {
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

    fun getBucketsByField(aggregationName: String,  fieldName: String): List<BucketDto> {
        val termAggregation = AggregationBuilders.terms(aggregationName).field(fieldName) // default size: 10
        val searchSourceBuilder = SearchSourceBuilder().aggregation(termAggregation)

        val searchRequest = SearchRequest().indices("my_stations").source(searchSourceBuilder)
        val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
        val terms = searchResponse.aggregations.get<Terms>(aggregationName)
        return terms.buckets.map {
            BucketDto(it.keyAsString, it.docCount)
        }
    }

}

class BucketDto(
    val key: String,
    val count: Long
)