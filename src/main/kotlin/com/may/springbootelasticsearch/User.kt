package com.may.springbootelasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "user") // index 테이블, @Entity와 동일하고, 몽고 DB의 @Document와 동일하다
class User(
    @Id 
    private val id: Long? = null,
    private val name: String,
    private val age: Int,
    private var email: String? = null
)