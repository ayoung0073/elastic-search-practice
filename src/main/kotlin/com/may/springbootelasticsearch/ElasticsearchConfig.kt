package com.may.springbootelasticsearch

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate

@Configuration
class ElasticsearchConfig : ElasticsearchConfigurationSupport() {

    @Bean
    fun elasticsearchClient(): RestHighLevelClient {
        val clientConfig = ClientConfiguration.builder()
            .connectedTo("localhost:9200")
            .build()

        return RestClients.create(clientConfig).rest()
    }

    @Bean
    fun elasticsearchTemplate() = ElasticsearchRestTemplate(elasticsearchClient())
}