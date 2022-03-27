package com.may.springbootelasticsearch.messagelog

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.stereotype.Repository

@Repository
class MessageLogCommandRepository(
    private val elasticsearchClient: RestHighLevelClient,
    private val objectMapper: ObjectMapper
) {
    fun saveMessageLog(message: MessageLog) {
        val indexRequest = Requests
            .indexRequest(makeIndex(message))
            .source(convertLog(message))
        elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT)
    }

    private fun convertLog(log: MessageLog): Map<String, Any> {
        val value = objectMapper.writeValueAsString(log)
        return objectMapper.readValue(value, object : TypeReference<Map<String, Any>>() {})
    }

    private fun makeIndex(log: MessageLog): String {
        return "${MessageLogConstant.INDEX_PATTERN}${log.createdAt.format(MessageLogConstant.INDEX_FORMATTER)}"
    }
}