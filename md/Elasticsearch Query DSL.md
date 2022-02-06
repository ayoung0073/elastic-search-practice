# Elasticsearch Query DSL

> #### 💡 시작하기 전 bulk로 데이터를 입력한다.


```kotlin
curl -XPOST "http://localhost:9200/my_index/_bulk" -H 'Content-Type: application/json' --data-binary @my_index.json
```

- my_index.json
    
    ```kotlin
    {"index":{"_id":1}}
    {"message":"The quick brown fox"}
    {"index":{"_id":2}}
    {"message":"The quick brown fox jumps over the lazy dog"}
    {"index":{"_id":3}}
    {"message":"The quick brown fox jumps over the quick dog"}
    {"index":{"_id":4}}
    {"message":"Brown fox brown dog"}
    {"index":{"_id":5}}
    {"message":"Lazy jumping dog"}
    ```
    

```kotlin
curl -XPOST "http://localhost:9200/phones/_bulk" -H 'Content-Type: application/json' --data-binary @phones.json
```

- phoes.json
    
    ```bash
    POST phones/_bulk
    {"index":{"_id":1}}
    {"model":"Samsung GalaxyS 5","price":475,"date":"2014-02-24"}
    {"index":{"_id":2}}
    {"model":"Samsung GalaxyS 6","price":795,"date":"2015-03-15"}
    {"index":{"_id":3}}
    {"model":"Samsung GalaxyS 7","price":859,"date":"2016-02-21"}
    {"index":{"_id":4}}
    {"model":"Samsung GalaxyS 8","price":959,"date":"2017-03-29"}
    {"index":{"_id":5}}
    {"model":"Samsung GalaxyS 9","price":1059,"date":"2018-02-25"}
    ```
    

Elasticsearch 는 데이터를 실제로 검색에 사용되는 검색어인 **텀(Term)** 으로 분석 과정을 거쳐 저장하기 때문에 검색 시 대소문자, 단수나 복수, 원형 여부와 상관 없이 검색이 가능하다. 이러한 Elasticsearch의 특징을 **풀 텍스트 검색(Full Text Search)** 이라고 한다.(한국어로 **전문 검색)**. 

Query DSL(Domain Specific Language)

ES의 Query DSL은 모두 json 형식으로 입력해야 한다.

## 풀 텍스트 쿼리(Full Text Query)

```bash
GET my_index/_search
{
  "query": {
    "match": {
      "message": "quick dog"
    }
  }
}
```

→ quick과 dog 중 어떤 단어라도 포함한 도큐먼트가 검색된다.

OR이 아닌 **AND**로 조건을 바꿀 경우, `operator` 옵션을 사용할 수 있다.

```bash
{
    "query": {
        "match": {
            "message": {
                "query": "quick dog", 
                "operator": "and"
            }
        }
    }
}
```

- **match_phrase**
    
    “quick dog”라는 구문을 공백을 포함해 정확한 내용을 검색하는 경우 사용
    
    ```bash
    {
      "query": {
        "match_phrase": {
          "message": "lazy dog"
        }
      }
    }
    ```
    
    ```bash
    // 같은 쿼리문 (query_string 이용)
    {
      "query": {
        "query_string": {
          "default_field": "message",
          "query": "\"lazy dog\""
        }
      }
    }
    ```
    
    이 쿼리는 `slop` 이라는 옵션을 이용해 `slop`에 지정된 값만큼 단어 사이에 다른 검색어가 끼어드는 것을 허용할 수 있다.
    
    - slop 1로 검색
        
        ```kotlin
        curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
        {
          "query": {
            "match_phrase": {
              "message": {
                "query": "lazy dog",
                "slop": 1
              }
            }
          }
        }
        '
        ```
        
        - response
            
            ```kotlin
            {
              "took" : 17,
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
                "max_score" : 1.0110221,
                "hits" : [
                  {
                    "_index" : "my_index",
                    "_type" : "_doc",
                    "_id" : "5",
                    "_score" : 1.0110221,
                    "_source" : {
                      "message" : "Lazy jumping dog"
                    }
                  },
                  {
                    "_index" : "my_index",
                    "_type" : "_doc",
                    "_id" : "2",
                    "_score" : 0.94896436,
                    "_source" : {
                      "message" : "The quick brown fox jumps over the lazy dog"
                    }
                  }
                ]
              }
            }
            ```
            
        
        lazy와 dog 사이에 하나의 단어도 검색을 허용한다.
        
- **query_string**
    
    URL검색에 사용하는 루씬의 검색 문법을 본문 검색에 이용하고 싶을 때 사용한다.
    
    ```kotlin
    curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "query_string": {
          "default_field": "message",
          "query": "(jumping AND lazy) OR \"quick dog\""
        }
      }
    }
    '
    ```
    
    - response
        
        ```kotlin
        {
          "took" : 63,
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
            "max_score" : 2.818369,
            "hits" : [
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "5",
                "_score" : 2.818369,
                "_source" : {
                  "message" : "Lazy jumping dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "3",
                "_score" : 0.67445135,
                "_source" : {
                  "message" : "The quick brown fox jumps over the quick dog"
                }
              }
            ]
          }
        }
        ```
        

## Bool 복합 쿼리(Bool Query)

query_string 쿼리는 여러 조건을 조합하기에는 용이한 문법이지만, 옵션이 한정되어 있다.

본문 검색에는 **여러 쿼리를 조합**하기 위해서는 상위에 `bool` 쿼리를 사용하고 그 안에 다른 쿼리들을 넣은 식으로 사용이 가능하다.

`bool` 쿼리는 다음의 4개 인자를 가지고 있고, 그 인자 안에 다른 쿼리들을 배열로 넣는다.

- must:  쿼리가 참인 도큐먼트들을 검색한다.
- must_not: 쿼리가 거짓인 도큐먼트들을 검색한다.
- should: 검색 결과 중 **이 쿼리에 해당하는 도큐먼트의 점수를 높인다.**
- filter: 쿼리가 참인 도큐먼트를 검색하지만 **스코어를 계산하지 않는다**. must보다 검색 속도가 빠르고 캐싱이 가능하다.

bool 쿼리를 이용해 **복합적인 검색 기능**을 구현할 수 있다. 

- 문법
    
    ```kotlin
    GET <인덱스명>/_search
    {
      "query": {
        "bool": {
          "must": [
            { <쿼리> }, …
          ],
          "must_not": [
            { <쿼리> }, …
          ],
          "should": [
            { <쿼리> }, …
          ],
          "filter": [
            { <쿼리> }, …
          ]
        }
      }
    }
    ```
    

- must로 “quick”과 “lazy dog”가 포함된 모든 문서를 검색하는 쿼리
    
    ```kotlin
    curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "bool": {
    			"must": [
    				{
    					"match": {
    						"message": "quick"
    					}	
    				},
    				{
    					"match_phrase": {
    						"message": "lazy dog"
    					}
    				}
    			]
        }
      }
    }
    '
    ```
    
    - 같은 쿼리
        
        ```bash
        curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
        {
          "query": {
            "bool": {
        			"must": [
        				{
        					"match": {
        						"message": "quick"
        					}	
        				},
        				{
        			    "query_string": {
        			      "default_field": "message",
        			      "query": "\"lazy dog\""
        			    }
        				}
        			]
            }
          }
        }
        '
        ```
        
    - response
        
        ```bash
        {
          "took" : 24,
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
            "max_score" : 1.3887084,
            "hits" : [
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "2",
                "_score" : 1.3887084,
                "_source" : {
                  "message" : "The quick brown fox jumps over the lazy dog"
                }
              }
            ]
          }
        }
        ```
        
- must_not으로 “quick”과 “lazy dog”가 포함되지 않은 문서 검색
    
    ```bash
    curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "bool": {
          "must_not": [
            {
              "match": {
                "message": "quick"
              }
            },
            {
              "match_phrase": {
                "message": "lazy dog"
              }
            }
          ]
        }
      }
    }
    '
    ```
    
    - response
        
        ```bash
        {
          "took" : 51,
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
            "max_score" : 0.0,
            "hits" : [
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "4",
                "_score" : 0.0,
                "_source" : {
                  "message" : "Brown fox brown dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "5",
                "_score" : 0.0,
                "_source" : {
                  "message" : "Lazy jumping dog"
                }
              }
            ]
          }
        }
        ```
        
    

## 정확도(Relevancy)

RDBMS 같은 시스템에서는 쿼리 조건에 부합하는지만 판단하여 결과를 가져오기만 하고, 각 결과들이 얼마나 정확한지에 대한 판단은 보통 불가능하다.

Elasticsearch는 검색 결과가 입력된 검색 조건과 얼마나 정확하게 일치하는지 계산하는 알고리즘을 가지고 있어, 이 정확도를 기반으로 사용자가 가장 원하는 결과를 먼저 보여줄 수 있다.

### 스코어

스코어는 검색된 결과가 얼마나 검색 조건과 일치하는지를 나타내며 점수가 높은 순으로 결과를 보여준다.

```bash
{
  "took" : 24,
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
    "max_score" : 1.3887084,
    "hits" : [
      {
        "_index" : "my_index",
        "_type" : "_doc",
        "_id" : "2",
        "_score" : 1.3887084,
        "_source" : {
          "message" : "The quick brown fox jumps over the lazy dog"
        }
      }
    ]
  }
}
```

ES는 스코어를 계산하기 위해 BM25라는 알고리즘을 이용한다. (BM: Best Matching)

- **TF(Term Frequency)**    
  도큐먼트 내에 검색된 **텀(term)**이 더 많을수록 점수가 높아지는 것을 **Term Frequency** 라고 한다.

- **IDF (Inverse Document Frequency)**
    검색한 텀을 포함하고 있는 도큐먼트 개수가 많을수록 그 텀의 자신의 점수가 감소하는 것을 **Inverse Document Frequency**라고 한다.
    
- **Field Length**
    
    도큐먼트에서 필드 길이가 큰 필드 보다는 짧은 필드에 있는 텀의 비중이 클 것이다. 블로그 포스트를 검색하는 경우 검색 하려는 단어가 **제목**과 **내용** 필드에 모두 있는 경우 텍스트 길이가 긴 **내용** 필드 보다는 텍스트 길이가 짧은 **제목** 필드에 검색어를 포함하고 있는 블로그 포스트가 더 점수가 높게 나타난다.
    
    ```bash
    curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "match": {
          "message": "lazy"
        }
      }
    }
    '
    ```
    
    - response hits field
        
        ```bash
        "hits" : [
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "5",
                "_score" : 1.0909162,
                "_source" : {
                  "message" : "Lazy jumping dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "2",
                "_score" : 0.714257,
                "_source" : {
                  "message" : "The quick brown fox jumps over the lazy dog"
                }
              }
            ]
        ```
        
        길이가 짧은 내용이 점수가 더 높게 나타난다.
        

## bool 쿼리의 should

bool 쿼리의 should는 검색 결과 중 **이 쿼리에 해당하는 도큐먼트의 점수를 조정할 수 있다.**

먼저 should 없이 도큐먼트를 검색해보자

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "message": "fox"
    }
  }
}
'
```

- response hits field
    
    ```bash
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "The quick brown fox"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "Brown fox brown dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the lazy dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the quick dog"
            }
          }
        ]
    ```
    

이제 `lazy` 가 포함된 결과에 가중치를 줘서 상위를 올려보자.

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        }
      ],
      "should": [
        {
          "match": {
            "message": "lazy"
          }
        }
      ]
    }
  }
}
'
```

- response hits field
    
    `lazy`를 포함하고 있는 "The quick brown fox jumps over the lazy dog"는 점수가 가중되어 가장 상위에 나타나게 된다.
    
    ```bash
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 0.94896436,
            "_source" : {
              "message" : "The quick brown fox jumps over the lazy dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "The quick brown fox"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "Brown fox brown dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the quick dog"
            }
          }
        ]
    ```
    

이번엔 lazy 또는 dog 중 하나라도 포함된 도큐먼트를 모두 검색하고, 그 중 “lazy dog” 구문을 정확히 포한하는 결과를 가장 상위로 가져오게 하자.

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "lazy dog"
          }
        }
      ],
      "should": [
        {
          "match_phrase": {
            "message": "lazy dog"
          }
        }
      ]
    }
  }
}
'
```

- response hits field
    
    ```bash
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 1.8979287,
            "_source" : {
              "message" : "The quick brown fox jumps over the lazy dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "5",
            "_score" : 1.4493951,
            "_source" : {
              "message" : "Lazy jumping dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "Brown fox brown dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the quick dog"
            }
          }
        ]
    ```
    

## 정확값 쿼리(Exact Value Query)

정확도를 고려하는 것 외에도 검색 조건의 참/거짓 여부만을 판별해서 결과를 가져오는 것이 가능하다.

Exact Value 에는 **term**, **range**와 같은 쿼리들이 이 부분에 속하며, 스코어를 계산하지 않기 때문에 보통 **bool** 쿼리의 **filter** 내부에서 사용하게 된다.

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "message": "fox"
    }
  }
}
'
```

- response hits field
    
    ```json
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "The quick brown fox"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "Brown fox brown dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the lazy dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the quick dog"
            }
          }
        ]
    ```
    

must로 fox 검색 및 filter로 quick 필터링

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        }
      ],
      "filter": [
        {
          "match": {
            "message": "quick"
          }
        }
      ]
    }
  }
}
'
```

- response hits field
    
    filter는 검색에 조건은 추가하지만 스코어에는 영향을 주지 않도록 하고, “quick”이 없는 내용은 제외된다.
    
    ```bash
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "1",
            "_score" : 0.32951736,
            "_source" : {
              "message" : "The quick brown fox"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the lazy dog"
            }
          },
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 0.23470736,
            "_source" : {
              "message" : "The quick brown fox jumps over the quick dog"
            }
          }
        ]
    ```
    

### keyword

문자열 데이터는 keyword 형식으로 저장하여 정확값 검색이 가능하다. 다음 쿼리는 message 필드값이 **"Brown fox brown dog"** 문자열과 공백, 대소문자까지 정확히 일치하는 데이터만을 결과로 리턴한다.

- keyword를 사용 안하는 경우
    
    ```bash
    curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "bool": {
          "filter": [
            {
              "match": {
                "message": "Brown fox brown dog"
              }
            }
          ]
        }
      }
    }
    '
    ```
    
    - response
        
        ```bash
        "hits" : [
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "1",
                "_score" : 0.0,
                "_source" : {
                  "message" : "The quick brown fox"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "2",
                "_score" : 0.0,
                "_source" : {
                  "message" : "The quick brown fox jumps over the lazy dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "3",
                "_score" : 0.0,
                "_source" : {
                  "message" : "The quick brown fox jumps over the quick dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "4",
                "_score" : 0.0,
                "_source" : {
                  "message" : "Brown fox brown dog"
                }
              },
              {
                "_index" : "my_index",
                "_type" : "_doc",
                "_id" : "5",
                "_score" : 0.0,
                "_source" : {
                  "message" : "Lazy jumping dog"
                }
              }
            ]
        ```
        

```bash
curl -XGET 'localhost:9200/my_index/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "filter": [
        {
          "match": {
            "message.keyword": "Brown fox brown dog"
          }
        }
      ]
    }
  }
}
'
```

- response hits field
    
    ```bash
    "hits" : [
          {
            "_index" : "my_index",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 0.0,
            "_source" : {
              "message" : "Brown fox brown dog"
            }
          }
        ]
    ```
    

## 범위 쿼리(Range Query)

Elasticsearch는 이 외에도 **숫자**나 **날짜** 형식들의 저장이 가능하다. 숫자, 날짜 형식은 **range** 쿼리를 이용해서 검색을 한다.

range 쿼리는 `range : { <필드명>: { <파라메터>:<값> } }` 으로 입력된다.

- **gte** (Greater-than or equal to)
- **gt** (Greater-than)
- **lte** (Less-than or equal to)
- **lt** (Less-than)

phoes 인덱스에서 price 필드 값에 range를 두어 검색해보자

```bash
curl -XGET 'localhost:9200/phones/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "range": {
      "price": {
        "gte": 700,
        "lt": 900
      }
    }
  }
}
'
```

- response hits field
    
    ```bash
    "hits" : [
          {
            "_index" : "phones",
            "_type" : "_doc",
            "_id" : "2",
            "_score" : 1.0,
            "_source" : {
              "model" : "Samsung GalaxyS 6",
              "price" : 795,
              "date" : "2015-03-15"
            }
          },
          {
            "_index" : "phones",
            "_type" : "_doc",
            "_id" : "3",
            "_score" : 1.0,
            "_source" : {
              "model" : "Samsung GalaxyS 7",
              "price" : 859,
              "date" : "2016-02-21"
            }
          }
        ]
    ```
    

### 날짜 검색

날짜도 숫자와 마찬가지로 **range** 쿼리의 사용이 가능하다. 기본적으로 Elasticsearch 에서 날짜 값은 `2016-01-01` 또는 `2016-01-01T10:15:30` 과 같이 JSON 에서 일반적으로 사용되는 **ISO8601** 형식을 사용한다. 

date 필드의 날짜가 2017-03-29 이후인 도큐먼트들을 검색해보자.

```bash
curl -XGET 'localhost:9200/phones/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": {
    "range": {
      "date": {
        "gte": "2017-03-29"
      }
    }
  }
}
'
```

- response hits field
    
    ```bash
    "hits" : [
          {
            "_index" : "phones",
            "_type" : "_doc",
            "_id" : "4",
            "_score" : 1.0,
            "_source" : {
              "model" : "Samsung GalaxyS 8",
              "price" : 959,
              "date" : "2017-03-29"
            }
          },
          {
            "_index" : "phones",
            "_type" : "_doc",
            "_id" : "5",
            "_score" : 1.0,
            "_source" : {
              "model" : "Samsung GalaxyS 9",
              "price" : 1059,
              "date" : "2018-02-25"
            }
          }
        ]
    ```
    
- `format` 옵션
    
    `||`을 사용해서 여러 값의 입력이 가능하다. 
    
    다음은 date 필드의 값이 2016-12-31부터 2018 이전 사이에 있는 값들을 검색하는 쿼리다.
    
    ```bash
    curl -XGET 'localhost:9200/phones/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "range": {
          "date": {
            "gt": "31/12/2016",
            "lt": "2018",
            "format": "dd/MM/yyyy||yyyy"
          }
        }
      }
    }
    '
    ```
    
    - response hits field
        
        ```bash
        "hits" : [
              {
                "_index" : "phones",
                "_type" : "_doc",
                "_id" : "4",
                "_score" : 1.0,
                "_source" : {
                  "model" : "Samsung GalaxyS 8",
                  "price" : 959,
                  "date" : "2017-03-29"
                }
              }
            ]
        ```
        
- 날짜를 검색할 때 `now` 예약어를 사용할 수 있다. ( + y, M, d, h, m, s, w)
    
    다음은 date의 값이 2016-1-1에서 6개월 후인 날부터 오늘보다 365일 전인 날 사이의 데이터를 가져오는 쿼리다.
    
    ```bash
    curl -XGET 'localhost:9200/phones/_search?pretty' -H 'Content-Type: application/json' -d'
    {
      "query": {
        "range": {
          "date": {
            "gt": "2016-01-01||+6M",
            "lt": "now-365d"
          }
        }
      }
    }
    '
    ```
    
    - response hits field
        
        ```bash
        "hits" : [
              {
                "_index" : "phones",
                "_type" : "_doc",
                "_id" : "4",
                "_score" : 1.0,
                "_source" : {
                  "model" : "Samsung GalaxyS 8",
                  "price" : 959,
                  "date" : "2017-03-29"
                }
              },
              {
                "_index" : "phones",
                "_type" : "_doc",
                "_id" : "5",
                "_score" : 1.0,
                "_source" : {
                  "model" : "Samsung GalaxyS 9",
                  "price" : 1059,
                  "date" : "2018-02-25"
                }
              }
            ]
        ```
        
    


> #### 💡 참고 링크


[5. 검색과 쿼리 - Query DSL](https://esbook.kimjmin.net/05-search)
