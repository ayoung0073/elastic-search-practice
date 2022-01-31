# Elasticsearch REST API 사용하기

## Elasticsearch document, index 생성

Elasticsearch는 인덱스를 미리 생성해도 되고, 인덱스와 타입을 동시에 생성하면서 하나의 Document를 생성할 수 있다.

### index 생성하기

```bash
curl -XPUT 'localhost:9200/index1?pretty'

{
  "acknowledged" : true,
  "shards_acknowledged" : true,
  "index" : "index1"
}
```

### Index에 document 추가하기

문서를 색인화하려면 어떤 type인지, 몇 번 _id에 색인화할 것인지 명시해줘야 한다.

- 1번 _id에 색인화하는 작업

    ```bash
    curl -XPOST "http://localhost:9200/index1/_doc/1?pretty" -H 'Content-Type: application/json' -d'
    {
      "name": "Ayoung Moon",
      "message": "안녕하세요 Elasticsearch"
    }'
    
    {
      "_index" : "index1",
      "_type" : "_doc",
      "_id" : "1",
      "_version" : 2,
      "result" : "updated",
      "_shards" : {
        "total" : 2,
        "successful" : 1,
        "failed" : 0
      },
      "_seq_no" : 1,
      "_primary_term" : 1
    }
    ```

- _id를 명시하지 않고 document를 추가하는 경우

    ```bash
    curl -XPOST "http://localhost:9200/index1/_doc?pretty" -H 'Content-Type: application/json' -d'
    {
      "name": "Maong Moon",
      "message": "안녕하세요 Maong ㅎㅎ"
    }'
    
    {
      "_index" : "index1",
      "_type" : "_doc",
      "_id" : "UpHdpn4BY3_wgTqhcdhb",
      "_version" : 1,
      "result" : "created",
      "_shards" : {
        "total" : 2,
        "successful" : 1,
        "failed" : 0
      },
      "_seq_no" : 2,
      "_primary_term" : 1
    }
    ```

  임의의 문자열을 _id로 할당된다.


## 모든 document 조회하기

```bash
curl -XGET 'localhost:9200/index1/_search?pretty'
```

- response

    ```bash
    {
      "took" : 976,
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
        "max_score" : 1.0,
        "hits" : [
          {
            "_index" : "index1",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 1.0,
            "_source" : {
              "name" : "Ayoung Moon",
              "message" : "안녕하세요 Elasticsearch"
            }
          },
          {
            "_index" : "index1",
            "_type" : "_doc",
            "_id" : "UpHdpn4BY3_wgTqhcdhb",
            "_score" : 1.0,
            "_source" : {
              "name" : "Maong Moon",
              "message" : "안녕하세요 Maong ㅎㅎ"
            }
          }
        ]
      }
    }
    ```


## 모든 index에 대한 모든 document 조회하기

```bash
curl -XGET 'localhost:9200/_all/_search?pretty'
```

- response

    ```bash
    {
      "took" : 39,
      "timed_out" : false,
      "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {
        "total" : {
          "value" : 4,
          "relation" : "eq"
        },
        "max_score" : 1.0,
        "hits" : [
          {
            "_index" : "index1",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 1.0,
            "_source" : {
              "name" : "Ayoung Moon",
              "message" : "안녕하세요 Elasticsearch"
            }
          },
          {
            "_index" : "index1",
            "_type" : "_doc",
            "_id" : "UpHdpn4BY3_wgTqhcdhb",
            "_score" : 1.0,
            "_source" : {
              "name" : "Maong Moon",
              "message" : "안녕하세요 Maong ㅎㅎ"
            }
          },
          {
            "_index" : "target_group",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 1.0,
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
            }
          }
        ]
      }
    }
    ```


## _id를 이용해 document 조회

```bash
curl -XGET "http://localhost:9200/index1/_doc/UpHdpn4BY3_wgTqhcdhb"
```

- response

    ```bash
    {
      "_index" : "index1",
      "_type" : "_doc",
      "_id" : "UpHdpn4BY3_wgTqhcdhb",
      "_version" : 1,
      "_seq_no" : 2,
      "_primary_term" : 1,
      "found" : true,
      "_source" : {
        "name" : "Maong Moon",
        "message" : "안녕하세요 Maong ㅎㅎ"
      }
    }
    ```


## document 수정

```bash
curl -XPUT 'localhost:9200/index1/_doc/UpHdpn4BY3_wgTqhcdhb?pretty' -H 'Content-Type: application/json' -d '
{
  "name": "Maong",
  "message": "안녕하세요 수정했어요!"
}'
```

- response

    ```bash
    {
      "_index" : "index1",
      "_type" : "_doc",
      "_id" : "UpHdpn4BY3_wgTqhcdhb",
      "_version" : 2, # version up
      "result" : "updated",
      "_shards" : {
        "total" : 2,
        "successful" : 1,
        "failed" : 0
      },
      "_seq_no" : 3,
      "_primary_term" : 1
    }
    ```


## document, index 삭제

- document 삭제

    ```bash
    curl -XDELETE 'localhost:9200/index1/_doc/1?pretty'
    {
      "_index" : "index1",
      "_type" : "_doc",
      "_id" : "1",
      "_version" : 3,
      "result" : "deleted",
      "_shards" : {
        "total" : 2,
        "successful" : 1,
        "failed" : 0
      },
      "_seq_no" : 5,
      "_primary_term" : 1
    }
    ```

- index 삭제

    ```bash
    curl -XDELETE 'localhost:9200/index1?pretty'
    {
      "acknowledged" : true
    }
    ```