package com.may.springbootelasticsearch.messagelog

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Repository

@Repository
class MessageLogQueryRepository(
    private val elasticsearchClient: RestHighLevelClient,
) {
    fun getMessageLogs(searchRequest: MessageLogSearchRequest): SearchResponse {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(makeQuery(searchRequest))

        val searchRequest =
            SearchRequest().indices("${MessageLogConstant.INDEX_PATTERN}${searchRequest.date}").source(searchSourceBuilder)
        return elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
    }

    fun getMessageLogsByUserId(searchRequest: MessageLogSearchRequest): List<BucketDto> {
        val termAggregation = AggregationBuilders.terms(AGGREGATION_NAME).field("userId.id") // default size: 10
        val searchSourceBuilder = SearchSourceBuilder()
            .aggregation(termAggregation)
            .query(makeQuery(searchRequest))

        val searchRequest =
            SearchRequest().indices("${MessageLogConstant.INDEX_PATTERN}${searchRequest.date}").source(searchSourceBuilder)
        val searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
        val terms = searchResponse.aggregations.get<Terms>(AGGREGATION_NAME)
        return terms.buckets.map {
            BucketDto(it.keyAsString, it.docCount)
        }
    }

    private fun makeQuery(filter: MessageLogSearchRequest): BoolQueryBuilder? {
        val queryBuilder = QueryBuilders.boolQuery()
        filter.message?.let {
            queryBuilder.filter(QueryBuilders.termQuery("message", it))
        }
        filter.status?.let {
            queryBuilder.filter(QueryBuilders.matchQuery("status", it))
            // TODO termQuery는 쿼리가 안됨
        }
        filter.userId?.let {
            queryBuilder.filter(QueryBuilders.termQuery("userId.id", it))
        }
        return queryBuilder
    }


    companion object {
        const val AGGREGATION_NAME = "message_log"
    }
}

class BucketDto(
    val key: String,
    val count: Long
)