package com.may.springbootelasticsearch.user

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : ElasticsearchRepository<User, Long>, UserCustomRepository

interface UserCustomRepository {
//    fun bulkInsert(userList: List<String>)
}

class UserEsCustomRepositorImpl(
    private val client: RestHighLevelClient
) : UserCustomRepository {
//    override fun bulkInsert(userList: List<String>) {
//        val request = BulkRequest()
//        for (user in userList) {
//            request.add(IndexRequest("user", "", user))
//        }
//        client.bulk(request, RequestOptions.DEFAULT)
//    }
}