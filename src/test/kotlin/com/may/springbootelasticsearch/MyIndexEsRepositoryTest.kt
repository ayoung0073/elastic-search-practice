package com.may.springbootelasticsearch

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
internal class MyIndexEsRepositoryTest @Autowired constructor(
    private val repository: MyIndexEsRepository
) {
    @Test
    fun `메세지에 dog가 포함된 도큐먼트를 가져온다`() {
        val response = repository.getMessagesByTerm("dog")
        /*
            {"took":78,"timed_out":false,
            "_shards":{"total":2,"successful":2,"skipped":0,"failed":0},
            "hits":{"total":{"value":4,"relation":"eq"},"max_score":0.0,
            "hits":[
            {"_index":"my_index","_type":"_doc","_id":"2","_score":0.0,"_source":{"message":"The quick brown fox jumps over the lazy dog"}},
            {"_index":"my_index","_type":"_doc","_id":"3","_score":0.0,"_source":{"message":"The quick brown fox jumps over the quick dog"}},
            {"_index":"my_index","_type":"_doc","_id":"4","_score":0.0,"_source":{"message":"Brown fox brown dog"}},
            {"_index":"my_index","_type":"_doc","_id":"5","_score":0.0,"_source":{"message":"Lazy jumping dog"}}]}}
         */
        response.hits?.hits?.forEach {
            println(it.sourceAsMap["message"])
        }
        /*
            The quick brown fox jumps over the lazy dog
            The quick brown fox jumps over the quick dog
            Brown fox brown dog
            Lazy jumping dog
         */
    }

    @Test
    fun `station 별 버킷을 가져온다`() {
        val response = repository.getBucketsByField("stations", "station.keyword")
        for (bucketDto in response) {
            println("${bucketDto.key}: ${bucketDto.count}")
        }
        /*
            신도림: 3
            강남: 2
            불광: 1
            신촌: 1
            양재: 1
            종각: 1
            홍제: 1
         */
    }

    @Test
    fun `line 별 버킷을 가져온다`() {
        val response = repository.getBucketsByField("lines", "line.keyword")
        for (bucketDto in response) {
            println("${bucketDto.key}: ${bucketDto.count}")
        }
    }

    @Test
    fun `메세지 로그를 저장한다`() {
        repository.saveMessage(
            MessageLog(
                message = "test message",
                createdAt = LocalDateTime.now()
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