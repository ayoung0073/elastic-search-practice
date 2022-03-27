package com.may.springbootelasticsearch

import com.may.springbootelasticsearch.user.User
import com.may.springbootelasticsearch.user.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository
) {
    @Test
    fun saveTest() {
        val user = User(
            name = "문다영",
            age = 25
        )
        userRepository.save(user)
    }
}
