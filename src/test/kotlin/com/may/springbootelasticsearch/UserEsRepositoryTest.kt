package com.may.springbootelasticsearch

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserEsRepositoryTest @Autowired constructor(
    val userEsRepository: UserEsRepository
) {
    @Test
    fun saveTest() {
        val user = User(
            id = 3L,
            name = "문아영",
            age = 22
        )
        userEsRepository.save(user)
    }
}