# Elasticsearch Aggregations

## Bucket Aggregations
Bucket aggregation은 주어진 조건으로부터 버킷들을 만들고, 각 버킷에 소속되는 도큐먼트들을 모아 그룹으로 구분하는 것이다.

버킷 별로 포함되는 도큐먼트의 개수는 doc_count 값에 기본적으로 표시가 되며 각 버킷 안에 metrics aggregation을 이용해서 다른 계산도 가능하다.

Range, Histogram, Terms 등이 있다.

```bash
curl -XPOST "http://localhost:9200/my_stations/_bulk?pretty" -H 'Content-Type: application/json' --data-binary @my_stations.json
```

- my_stations.json

    ```json
    {"index": {"_id": "1"}}
    {"date": "2019-06-01", "line": "1호선", "station": "종각", "passangers": 2314}
    {"index": {"_id": "2"}}
    {"date": "2019-06-01", "line": "2호선", "station": "강남", "passangers": 5412}
    {"index": {"_id": "3"}}
    {"date": "2019-07-10", "line": "2호선", "station": "강남", "passangers": 6221}
    {"index": {"_id": "4"}}
    {"date": "2019-07-15", "line": "2호선", "station": "강남", "passangers": 6478}
    {"index": {"_id": "5"}}
    {"date": "2019-08-07", "line": "2호선", "station": "강남", "passangers": 5821}
    {"index": {"_id": "6"}}
    {"date": "2019-08-18", "line": "2호선", "station": "강남", "passangers": 5724}
    {"index": {"_id": "7"}}
    {"date": "2019-09-02", "line": "2호선", "station": "신촌", "passangers": 3912}
    {"index": {"_id": "8"}}
    {"date": "2019-09-11", "line": "3호선", "station": "양재", "passangers": 4121}
    {"index": {"_id": "9"}}
    {"date": "2019-09-20", "line": "3호선", "station": "홍제", "passangers": 1021}
    {"index": {"_id": "10"}}
    {"date": "2019-10-01", "line": "3호선", "station": "불광", "passangers": 971}
    ```



### Terms

terms aggregation은 keyword 필드의 문자열 별로 버킷을 나누어 집계가 가능하다. keyword 필드 값으로만 사용이 가능하며 분석된 text 필드는 일반적으로 사용이 불가능하다.

- terms를 이용해서 station 값 별로 버킷 생성

    ```kotlin
    curl -XGET "http://localhost:9200/my_stations/_search?pretty"  -H 'Content-Type: application/json' -d'
    {
      "size": 0,
      "aggs": {
        "station": {
          "terms": {
            "field": "station.keyword"
          }
        }
      }
    }'
    ```

    - response

        ```bash
        {
          "took" : 71,
          "timed_out" : false,
          "_shards" : {
            "total" : 1,
            "successful" : 1,
            "skipped" : 0,
            "failed" : 0
          },
          "hits" : {
            "total" : {
              "value" : 10,
              "relation" : "eq"
            },
            "max_score" : null,
            "hits" : [ ]
          },
          "aggregations" : {
            "station" : {
              "doc_count_error_upper_bound" : 0,
              "sum_other_doc_count" : 0,
              "buckets" : [
                {
                  "key" : "강남",
                  "doc_count" : 5
                },
                {
                  "key" : "불광",
                  "doc_count" : 1
                },
                {
                  "key" : "신촌",
                  "doc_count" : 1
                },
                {
                  "key" : "양재",
                  "doc_count" : 1
                },
                {
                  "key" : "종각",
                  "doc_count" : 1
                },
                {
                  "key" : "홍제",
                  "doc_count" : 1
                }
              ]
            }
          }
        }
        ```


- line 값 별로 버킷 생성

```bash
curl -XGET "http://localhost:9200/my_stations/_search?pretty"  -H 'Content-Type: application/json' -d'
{
  "size": 0,
  "aggs": {
    "line": {
      "terms": {
        "field": "line.keyword"
      }
    }
  }
}'
```

- response (aggregations)

    ```bash
    "aggregations" : {
        "line" : {
          "doc_count_error_upper_bound" : 0,
          "sum_other_doc_count" : 0,
          "buckets" : [
            {
              "key" : "2호선",
              "doc_count" : 6
            },
            {
              "key" : "3호선",
              "doc_count" : 3
            },
            {
              "key" : "1호선",
              "doc_count" : 1
            }
          ]
        }
    ```