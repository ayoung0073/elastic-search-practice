package com.may.springbootelasticsearch.messagelog

import java.time.format.DateTimeFormatter

class MessageLogConstant {
    companion object {
        const val INDEX_PATTERN = "message-log-"
        val INDEX_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}