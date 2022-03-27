package com.may.springbootelasticsearch

import com.may.springbootelasticsearch.messagelog.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
internal class MessageLogRepositoryTest @Autowired constructor(
    private val messageLogQueryRepository: MessageLogQueryRepository,
    private val messageLogCommandRepository: MessageLogCommandRepository
) {
    @Test
    fun `메세지가 포함된 메세지 로그 도큐먼트를 가져온다`() {
        val response = messageLogQueryRepository.getMessageLogs(
            MessageLogSearchRequest(
                date = LocalDate.now(),
                message = "test"
            )
        )

        response.hits?.hits?.forEach {
            println(it.sourceAsMap["message"])
        }
        /*
            test message created
            test message updated
            test message created
         */
    }
    @Test
    fun `status가 UPDATED인 메세지 로그 도큐먼트를 가져온다`() {
        val response = messageLogQueryRepository.getMessageLogs(
            MessageLogSearchRequest(
                date = LocalDate.now(),
                status = MessageStatus.UPDATED
            )
        )
        response.hits?.hits?.forEach {
            println(it.sourceAsMap["message"])
        }
        /*
            test message updated
            message updated
            test message updated
            test message updated
         */
    }

    @Test
    fun `userId별 버킷을 가져온다`() {
        val response = messageLogQueryRepository.getMessageLogsByUserId(
            MessageLogSearchRequest(
                date = LocalDate.now(),
                message = "test"
            )
        )
        for (bucketDto in response) {
            println("${bucketDto.key}: ${bucketDto.count}")
        }
        /*
            1: 2
            2: 1
         */
    }

    @Test
    fun `status에 따른 userId별 버킷을 가져온다`() {
        val response = messageLogQueryRepository.getMessageLogsByUserId(
            MessageLogSearchRequest(
                date = LocalDate.now(),
                status = MessageStatus.UPDATED
            )
        )
        for (bucketDto in response) {
            println("${bucketDto.key}: ${bucketDto.count}")
        }
    }

    @Test
    fun `메세지 로그를 저장한다`() {
        messageLogCommandRepository.saveMessageLog(
            MessageLog(
                message = "test message updated",
                createdAt = LocalDateTime.now(),
                status = MessageStatus.UPDATED,
                userId = UserId(2L)
            )
        )
        /*
            curl -XGET 'localhost:9200/message-log-2022-03-27/_search?pretty'
            {
              "took" : 24,
              "timed_out" : false,
              "_shards" : {
                "total" : 1,
                "successful" : 1,
                "skipped" : 0,
                "failed" : 0
              },
              "hits" : {
                "total" : {
                  "value" : 1,
                  "relation" : "eq"
                },
                "max_score" : 1.0,
                "hits" : [
                  {
                    "_index" : "message-log-2022-03-27",
                    "_type" : "_doc",
                    "_id" : "8aIcy38BtTirsXuYK13T",
                    "_score" : 1.0,
                    "_source" : {
                      "message" : "test message",
                      "createdAt" : "2022-03-27T20:21:35.135"
                    }
                  }
                ]
              }
            }
         */
    }
}