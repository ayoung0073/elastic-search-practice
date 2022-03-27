package com.may.springbootelasticsearch.messagelog

import java.time.LocalDate

data class MessageLogSearchRequest(
    val message: String? = null,
    val status: MessageStatus? = null,
    val userId: Long? = null,
    val date: LocalDate
)