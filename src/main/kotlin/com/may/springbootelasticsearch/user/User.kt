package com.may.springbootelasticsearch.user

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field

@Document(indexName = "user") // index 테이블, @Entity와 동일하고, 몽고 DB의 @Document와 동일하다
class User(
    @Id
    val id: Long? = null,
    @Field("name")
    val name: String,
    @Field("age")
    val age: Int,
    @Field("email")
    var email: String? = null
) {
    fun getJsonData() =
        mapOf("name" to this.name, "age" to this.age)
}