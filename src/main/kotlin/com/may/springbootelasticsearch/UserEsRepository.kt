package com.may.springbootelasticsearch

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface UserEsRepository: ElasticsearchRepository<User, Long>