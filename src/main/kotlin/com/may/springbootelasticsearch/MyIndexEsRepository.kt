package com.may.springbootelasticsearch

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Repository
import java.time.format.DateTimeFormatter

@Repository
class MyIndexEsRepository(
    private val elasticsearchClient: RestHighLevelClient,
    private val objectMapper: ObjectMapper
) {
    fun saveMessage(message: MessageLog) {
        val indexRequest = Requests
            .indexRequest(makeIndex(message))
            .source(convertLog(message))
        elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT)
    }

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

    private fun convertLog(log: MessageLog): Map<String, Any> {
        val value = objectMapper.writeValueAsString(log)
        return objectMapper.readValue(value, object : TypeReference<Map<String, Any>>() {})
    }
    private fun makeIndex(log: MessageLog): String {
        return "$INDEX_PATTERN${log.createdAt.format(INDEX_FORMATTER)}"
    }

    companion object {
        const val INDEX_PATTERN = "message-log-"
        val INDEX_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

class BucketDto(
    val key: String,
    val count: Long
)