package com.may.springbootelasticsearch.messagelog

import java.time.LocalDateTime

data class MessageLog(
    val message: String,
    val createdAt: LocalDateTime,
    val status: MessageStatus,
    val userId: UserId
)

enum class MessageStatus {
    CREATED, UPDATED, DELETED
}

data class UserId(
    val id: Long
)