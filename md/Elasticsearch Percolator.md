# Elasticsearch Percolator
## 대량의 스트림 데이터를 실시간으로 분류하기
###  Elasticsearch Percolator를 이용한 콘텐츠 분류 
쿼리를 등록해주고 도큐먼트를 담은 퍼컬레이트 요청을 보내 매칭된 쿼리를 반환해주는 Elasticsearch의 기능이다.

Elasticsearch에서 일반적인 검색 기능은 특정 인덱스에 문서를 저장하고, 쿼리에 매칭되는 문서를 불러오는 방식으로 수행된다.
하지만 percolate 쿼리 방식은 그 반대로 동작한다. 
쿼리를 사전에 저장하고, 새로 유입된 문서가 매칭되는 쿼리가 있는지 확인해 매칭되는 쿼리를 반환한다.

> #### percolator
> 1. 퍼컬레이터 2. 여과기 3. 여과하는 사람

### 실제로 퍼콜레이터 활용하는 방법
#### 1. Percolator Index 생성
Percolator Query를 저장하기 위해서는 쿼리가 저장될 인덱스를 생성해야 한다.
- 필터 조건으로 사용될 필드 정의
- 쿼리가 저장될 필드 정의
```bash
curl -X PUT "localhost:9200/target_group" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "follower_count": {
        "type": "integer"
      },
      "post_count": {
        "type": "integer"
      },
      "review_count": {
        "type": "integer"
      },
      "business_account_extra_rate": {
        "type": "integer"
      },
      "query": {
        "type": "percolator"
      }
    }
  }
}
'

# {"acknowledged":true,"shards_acknowledged":true,"index":"target_group"}
```

#### 2. Percolator Query 등록 
- 앞서 생성한 인덱스에 인덱싱 

> 팔로워 수가 2명이고, 소식 작성 수가 0개인 계정을 타겟
```bash
curl -X PUT "localhost:9200/target_group/_doc/1" -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
        "must": [
            {
                "match": {
                  "follower_count": 2
                }
            },
                {
                "match": {
                  "post_count": 0
                }
            }
        ] 
    }
  }
}
'

# {"_index":"target_group","_type":"_doc","_id":"1","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":0,"_primary_term":1}
```
> 후기 개수 0개인 계정을 타겟
```bash
curl -X PUT "localhost:9200/target_group/_doc/2" -H 'Content-Type: application/json' -d'
{
  "query": {
      "bool": {
      "must": [
        {
            "match": {
              "review_count": 0
            }
        }
      ]
    }
  }
}
'

# {"_index":"target_group","_type":"_doc","_id":"2","_version":2,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":2,"_primary_term":1}
```

#### 3. Percolator Query 검색
> 팔로워 수가 4명이고, 소식 작성 수가 0개인 문서와 매칭되는 쿼리가 있는지 검색
```bash
curl -X GET "localhost:9200/target_group/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "percolate": {
      "field": "query",
      "document": {
        "follower_count": 4,
        "post_count": 0
      }
    }
  }
}
'
```
- response
```bash
{
  "took" : 27,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 0,
      "relation" : "eq"
    },
    "max_score" : null,
    "hits" : [ ]
  }
}
```
> 팔로워 수: 4명, 소식 작성 수: 0개, 후기 개수: 0개인 문서와 매칭되는 쿼리가 있는지 검색 
```bash
curl -X GET "localhost:9200/target_group/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "percolate": {
      "field": "query",
      "document": {
        "follower_count": 4,
        "post_count": 0,
        "review_count": 0
      }
    }
  }
}
'
```
```bash 
{
  "took" : 36,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "target_group",
        "_type" : "_doc",
        "_id" : "2",
        "_score" : 1.0,
        "_source" : {
          "query" : {
            "bool" : {
              "must" : [
                {
                  "match" : {
                    "review_count" : 0
                  }
                }
              ]
            }
          }
        },
        "fields" : {
          "_percolator_document_slot" : [
            0
          ]
        }
      }
    ]
  }
}
```

> 모두 매칭되는 문서에 대한 쿼리 검색
```bash
curl -X GET "localhost:9200/target_group/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "percolate": {
      "field": "query",
      "document": {
        "follower_count": 2,
        "post_count":0,
        "review_count":0
      }
    }
  }
}
'
```
- response
```bash
{
  "took" : 35,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 2,
      "relation" : "eq"
    },
    "max_score" : 2.0,
    "hits" : [
      {
        "_index" : "target_group",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : 2.0,
        "_source" : {
          "query" : {
            "bool" : {
              "must" : [
                {
                  "match" : {
                    "follower_count" : 2
                  }
                },
                {
                  "match" : {
                    "post_count" : 0
                  }
                }
              ]
            }
          }
        },
        "fields" : {
          "_percolator_document_slot" : [
            0
          ]
        }
      },
      {
        "_index" : "target_group",
        "_type" : "_doc",
        "_id" : "2",
        "_score" : 1.0,
        "_source" : {
          "query" : {
            "bool" : {
              "must" : [
                {
                  "match" : {
                    "review_count" : 0
                  }
                }
              ]
            }
          }
        },
        "fields" : {
          "_percolator_document_slot" : [
            0
          ]
        }
      }
    ]
  }
}
```

#### Perculator Query 결과 분석
- took: 걸린 시간(ms)
- hits.total.value: 매칭된 쿼리 수
- hits.hits: 매칭된 쿼리 목록



> 참고 링크
> - [if(kakao) 2021](https://if.kakao.com/session/53)
> - [Storm과 Elasticsearch Percolator를 이용한 NELO2 알람 기능 개선](https://d2.naver.com/helloworld/1044388) 읽어보기!