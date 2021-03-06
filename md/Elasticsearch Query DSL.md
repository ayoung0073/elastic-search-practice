# Elasticsearch Query DSL

> #### ๐ก ์์ํ๊ธฐ ์  bulk๋ก ๋ฐ์ดํฐ๋ฅผ ์๋ ฅํ๋ค.


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
    

Elasticsearch ๋ ๋ฐ์ดํฐ๋ฅผ ์ค์ ๋ก ๊ฒ์์ ์ฌ์ฉ๋๋ ๊ฒ์์ด์ธ **ํ(Term)** ์ผ๋ก ๋ถ์ ๊ณผ์ ์ ๊ฑฐ์ณ ์ ์ฅํ๊ธฐ ๋๋ฌธ์ ๊ฒ์ ์ ๋์๋ฌธ์, ๋จ์๋ ๋ณต์, ์ํ ์ฌ๋ถ์ ์๊ด ์์ด ๊ฒ์์ด ๊ฐ๋ฅํ๋ค. ์ด๋ฌํ Elasticsearch์ ํน์ง์ **ํ ํ์คํธ ๊ฒ์(Full Text Search)** ์ด๋ผ๊ณ  ํ๋ค.(ํ๊ตญ์ด๋ก **์ ๋ฌธ ๊ฒ์)**. 

Query DSL(Domain Specific Language)

ES์ Query DSL์ ๋ชจ๋ json ํ์์ผ๋ก ์๋ ฅํด์ผ ํ๋ค.

## ํ ํ์คํธ ์ฟผ๋ฆฌ(Full Text Query)

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

โ quick๊ณผ dog ์ค ์ด๋ค ๋จ์ด๋ผ๋ ํฌํจํ ๋ํ๋จผํธ๊ฐ ๊ฒ์๋๋ค.

OR์ด ์๋ **AND**๋ก ์กฐ๊ฑด์ ๋ฐ๊ฟ ๊ฒฝ์ฐ, `operator` ์ต์์ ์ฌ์ฉํ  ์ ์๋ค.

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
    
    โquick dogโ๋ผ๋ ๊ตฌ๋ฌธ์ ๊ณต๋ฐฑ์ ํฌํจํด ์ ํํ ๋ด์ฉ์ ๊ฒ์ํ๋ ๊ฒฝ์ฐ ์ฌ์ฉ
    
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
    // ๊ฐ์ ์ฟผ๋ฆฌ๋ฌธ (query_string ์ด์ฉ)
    {
      "query": {
        "query_string": {
          "default_field": "message",
          "query": "\"lazy dog\""
        }
      }
    }
    ```
    
    ์ด ์ฟผ๋ฆฌ๋ `slop` ์ด๋ผ๋ ์ต์์ ์ด์ฉํด `slop`์ ์ง์ ๋ ๊ฐ๋งํผ ๋จ์ด ์ฌ์ด์ ๋ค๋ฅธ ๊ฒ์์ด๊ฐ ๋ผ์ด๋๋ ๊ฒ์ ํ์ฉํ  ์ ์๋ค.
    
    - slop 1๋ก ๊ฒ์
        
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
            
        
        lazy์ dog ์ฌ์ด์ ํ๋์ ๋จ์ด๋ ๊ฒ์์ ํ์ฉํ๋ค.
        
- **query_string**
    
    URL๊ฒ์์ ์ฌ์ฉํ๋ ๋ฃจ์ฌ์ ๊ฒ์ ๋ฌธ๋ฒ์ ๋ณธ๋ฌธ ๊ฒ์์ ์ด์ฉํ๊ณ  ์ถ์ ๋ ์ฌ์ฉํ๋ค.
    
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
        

## Bool ๋ณตํฉ ์ฟผ๋ฆฌ(Bool Query)

query_string ์ฟผ๋ฆฌ๋ ์ฌ๋ฌ ์กฐ๊ฑด์ ์กฐํฉํ๊ธฐ์๋ ์ฉ์ดํ ๋ฌธ๋ฒ์ด์ง๋ง, ์ต์์ด ํ์ ๋์ด ์๋ค.

๋ณธ๋ฌธ ๊ฒ์์๋ **์ฌ๋ฌ ์ฟผ๋ฆฌ๋ฅผ ์กฐํฉ**ํ๊ธฐ ์ํด์๋ ์์์ `bool` ์ฟผ๋ฆฌ๋ฅผ ์ฌ์ฉํ๊ณ  ๊ทธ ์์ ๋ค๋ฅธ ์ฟผ๋ฆฌ๋ค์ ๋ฃ์ ์์ผ๋ก ์ฌ์ฉ์ด ๊ฐ๋ฅํ๋ค.

`bool` ์ฟผ๋ฆฌ๋ ๋ค์์ 4๊ฐ ์ธ์๋ฅผ ๊ฐ์ง๊ณ  ์๊ณ , ๊ทธ ์ธ์ ์์ ๋ค๋ฅธ ์ฟผ๋ฆฌ๋ค์ ๋ฐฐ์ด๋ก ๋ฃ๋๋ค.

- must:  ์ฟผ๋ฆฌ๊ฐ ์ฐธ์ธ ๋ํ๋จผํธ๋ค์ ๊ฒ์ํ๋ค.
- must_not: ์ฟผ๋ฆฌ๊ฐ ๊ฑฐ์ง์ธ ๋ํ๋จผํธ๋ค์ ๊ฒ์ํ๋ค.
- should: ๊ฒ์ ๊ฒฐ๊ณผ ์ค **์ด ์ฟผ๋ฆฌ์ ํด๋นํ๋ ๋ํ๋จผํธ์ ์ ์๋ฅผ ๋์ธ๋ค.**
- filter: ์ฟผ๋ฆฌ๊ฐ ์ฐธ์ธ ๋ํ๋จผํธ๋ฅผ ๊ฒ์ํ์ง๋ง **์ค์ฝ์ด๋ฅผ ๊ณ์ฐํ์ง ์๋๋ค**. must๋ณด๋ค ๊ฒ์ ์๋๊ฐ ๋น ๋ฅด๊ณ  ์บ์ฑ์ด ๊ฐ๋ฅํ๋ค.

bool ์ฟผ๋ฆฌ๋ฅผ ์ด์ฉํด **๋ณตํฉ์ ์ธ ๊ฒ์ ๊ธฐ๋ฅ**์ ๊ตฌํํ  ์ ์๋ค. 

- ๋ฌธ๋ฒ
    
    ```kotlin
    GET <์ธ๋ฑ์ค๋ช>/_search
    {
      "query": {
        "bool": {
          "must": [
            { <์ฟผ๋ฆฌ> }, โฆ
          ],
          "must_not": [
            { <์ฟผ๋ฆฌ> }, โฆ
          ],
          "should": [
            { <์ฟผ๋ฆฌ> }, โฆ
          ],
          "filter": [
            { <์ฟผ๋ฆฌ> }, โฆ
          ]
        }
      }
    }
    ```
    

- must๋ก โquickโ๊ณผ โlazy dogโ๊ฐ ํฌํจ๋ ๋ชจ๋  ๋ฌธ์๋ฅผ ๊ฒ์ํ๋ ์ฟผ๋ฆฌ
    
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
    
    - ๊ฐ์ ์ฟผ๋ฆฌ
        
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
        
- must_not์ผ๋ก โquickโ๊ณผ โlazy dogโ๊ฐ ํฌํจ๋์ง ์์ ๋ฌธ์ ๊ฒ์
    
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
        
    

## ์ ํ๋(Relevancy)

RDBMS ๊ฐ์ ์์คํ์์๋ ์ฟผ๋ฆฌ ์กฐ๊ฑด์ ๋ถํฉํ๋์ง๋ง ํ๋จํ์ฌ ๊ฒฐ๊ณผ๋ฅผ ๊ฐ์ ธ์ค๊ธฐ๋ง ํ๊ณ , ๊ฐ ๊ฒฐ๊ณผ๋ค์ด ์ผ๋ง๋ ์ ํํ์ง์ ๋ํ ํ๋จ์ ๋ณดํต ๋ถ๊ฐ๋ฅํ๋ค.

Elasticsearch๋ ๊ฒ์ ๊ฒฐ๊ณผ๊ฐ ์๋ ฅ๋ ๊ฒ์ ์กฐ๊ฑด๊ณผ ์ผ๋ง๋ ์ ํํ๊ฒ ์ผ์นํ๋์ง ๊ณ์ฐํ๋ ์๊ณ ๋ฆฌ์ฆ์ ๊ฐ์ง๊ณ  ์์ด, ์ด ์ ํ๋๋ฅผ ๊ธฐ๋ฐ์ผ๋ก ์ฌ์ฉ์๊ฐ ๊ฐ์ฅ ์ํ๋ ๊ฒฐ๊ณผ๋ฅผ ๋จผ์  ๋ณด์ฌ์ค ์ ์๋ค.

### ์ค์ฝ์ด

์ค์ฝ์ด๋ ๊ฒ์๋ ๊ฒฐ๊ณผ๊ฐ ์ผ๋ง๋ ๊ฒ์ ์กฐ๊ฑด๊ณผ ์ผ์นํ๋์ง๋ฅผ ๋ํ๋ด๋ฉฐ ์ ์๊ฐ ๋์ ์์ผ๋ก ๊ฒฐ๊ณผ๋ฅผ ๋ณด์ฌ์ค๋ค.

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

ES๋ ์ค์ฝ์ด๋ฅผ ๊ณ์ฐํ๊ธฐ ์ํด BM25๋ผ๋ ์๊ณ ๋ฆฌ์ฆ์ ์ด์ฉํ๋ค. (BM: Best Matching)

- **TF(Term Frequency)**    
  ๋ํ๋จผํธ ๋ด์ ๊ฒ์๋ **ํ(term)**์ด ๋ ๋ง์์๋ก ์ ์๊ฐ ๋์์ง๋ ๊ฒ์ **Term Frequency** ๋ผ๊ณ  ํ๋ค.

- **IDF (Inverse Document Frequency)**
    ๊ฒ์ํ ํ์ ํฌํจํ๊ณ  ์๋ ๋ํ๋จผํธ ๊ฐ์๊ฐ ๋ง์์๋ก ๊ทธ ํ์ ์์ ์ ์ ์๊ฐ ๊ฐ์ํ๋ ๊ฒ์ **Inverse Document Frequency**๋ผ๊ณ  ํ๋ค.
    
- **Field Length**
    
    ๋ํ๋จผํธ์์ ํ๋ ๊ธธ์ด๊ฐ ํฐ ํ๋ ๋ณด๋ค๋ ์งง์ ํ๋์ ์๋ ํ์ ๋น์ค์ด ํด ๊ฒ์ด๋ค. ๋ธ๋ก๊ทธ ํฌ์คํธ๋ฅผ ๊ฒ์ํ๋ ๊ฒฝ์ฐ ๊ฒ์ ํ๋ ค๋ ๋จ์ด๊ฐ **์ ๋ชฉ**๊ณผ **๋ด์ฉ** ํ๋์ ๋ชจ๋ ์๋ ๊ฒฝ์ฐ ํ์คํธ ๊ธธ์ด๊ฐ ๊ธด **๋ด์ฉ** ํ๋ ๋ณด๋ค๋ ํ์คํธ ๊ธธ์ด๊ฐ ์งง์ **์ ๋ชฉ** ํ๋์ ๊ฒ์์ด๋ฅผ ํฌํจํ๊ณ  ์๋ ๋ธ๋ก๊ทธ ํฌ์คํธ๊ฐ ๋ ์ ์๊ฐ ๋๊ฒ ๋ํ๋๋ค.
    
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
        
        ๊ธธ์ด๊ฐ ์งง์ ๋ด์ฉ์ด ์ ์๊ฐ ๋ ๋๊ฒ ๋ํ๋๋ค.
        

## bool ์ฟผ๋ฆฌ์ should

bool ์ฟผ๋ฆฌ์ should๋ ๊ฒ์ ๊ฒฐ๊ณผ ์ค **์ด ์ฟผ๋ฆฌ์ ํด๋นํ๋ ๋ํ๋จผํธ์ ์ ์๋ฅผ ์กฐ์ ํ  ์ ์๋ค.**

๋จผ์  should ์์ด ๋ํ๋จผํธ๋ฅผ ๊ฒ์ํด๋ณด์

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
    

์ด์  `lazy` ๊ฐ ํฌํจ๋ ๊ฒฐ๊ณผ์ ๊ฐ์ค์น๋ฅผ ์ค์ ์์๋ฅผ ์ฌ๋ ค๋ณด์.

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
    
    `lazy`๋ฅผ ํฌํจํ๊ณ  ์๋ "The quick brown fox jumps over the lazy dog"๋ ์ ์๊ฐ ๊ฐ์ค๋์ด ๊ฐ์ฅ ์์์ ๋ํ๋๊ฒ ๋๋ค.
    
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
    

์ด๋ฒ์ lazy ๋๋ dog ์ค ํ๋๋ผ๋ ํฌํจ๋ ๋ํ๋จผํธ๋ฅผ ๋ชจ๋ ๊ฒ์ํ๊ณ , ๊ทธ ์ค โlazy dogโ ๊ตฌ๋ฌธ์ ์ ํํ ํฌํํ๋ ๊ฒฐ๊ณผ๋ฅผ ๊ฐ์ฅ ์์๋ก ๊ฐ์ ธ์ค๊ฒ ํ์.

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
    

## ์ ํ๊ฐ ์ฟผ๋ฆฌ(Exact Value Query)

์ ํ๋๋ฅผ ๊ณ ๋ คํ๋ ๊ฒ ์ธ์๋ ๊ฒ์ ์กฐ๊ฑด์ ์ฐธ/๊ฑฐ์ง ์ฌ๋ถ๋ง์ ํ๋ณํด์ ๊ฒฐ๊ณผ๋ฅผ ๊ฐ์ ธ์ค๋ ๊ฒ์ด ๊ฐ๋ฅํ๋ค.

Exact Value ์๋ **term**, **range**์ ๊ฐ์ ์ฟผ๋ฆฌ๋ค์ด ์ด ๋ถ๋ถ์ ์ํ๋ฉฐ, ์ค์ฝ์ด๋ฅผ ๊ณ์ฐํ์ง ์๊ธฐ ๋๋ฌธ์ ๋ณดํต **bool** ์ฟผ๋ฆฌ์ **filter** ๋ด๋ถ์์ ์ฌ์ฉํ๊ฒ ๋๋ค.

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
    

must๋ก fox ๊ฒ์ ๋ฐ filter๋ก quick ํํฐ๋ง

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
    
    filter๋ ๊ฒ์์ ์กฐ๊ฑด์ ์ถ๊ฐํ์ง๋ง ์ค์ฝ์ด์๋ ์ํฅ์ ์ฃผ์ง ์๋๋ก ํ๊ณ , โquickโ์ด ์๋ ๋ด์ฉ์ ์ ์ธ๋๋ค.
    
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

๋ฌธ์์ด ๋ฐ์ดํฐ๋ keyword ํ์์ผ๋ก ์ ์ฅํ์ฌ ์ ํ๊ฐ ๊ฒ์์ด ๊ฐ๋ฅํ๋ค. ๋ค์ ์ฟผ๋ฆฌ๋ message ํ๋๊ฐ์ด **"Brown fox brown dog"** ๋ฌธ์์ด๊ณผ ๊ณต๋ฐฑ, ๋์๋ฌธ์๊น์ง ์ ํํ ์ผ์นํ๋ ๋ฐ์ดํฐ๋ง์ ๊ฒฐ๊ณผ๋ก ๋ฆฌํดํ๋ค.

- keyword๋ฅผ ์ฌ์ฉ ์ํ๋ ๊ฒฝ์ฐ
    
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
    

## ๋ฒ์ ์ฟผ๋ฆฌ(Range Query)

Elasticsearch๋ ์ด ์ธ์๋ **์ซ์**๋ **๋ ์ง** ํ์๋ค์ ์ ์ฅ์ด ๊ฐ๋ฅํ๋ค. ์ซ์, ๋ ์ง ํ์์ **range** ์ฟผ๋ฆฌ๋ฅผ ์ด์ฉํด์ ๊ฒ์์ ํ๋ค.

range ์ฟผ๋ฆฌ๋ `range : { <ํ๋๋ช>: { <ํ๋ผ๋ฉํฐ>:<๊ฐ> } }` ์ผ๋ก ์๋ ฅ๋๋ค.

- **gte** (Greater-than or equal to)
- **gt** (Greater-than)
- **lte** (Less-than or equal to)
- **lt** (Less-than)

phoes ์ธ๋ฑ์ค์์ price ํ๋ ๊ฐ์ range๋ฅผ ๋์ด ๊ฒ์ํด๋ณด์

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
    

### ๋ ์ง ๊ฒ์

๋ ์ง๋ ์ซ์์ ๋ง์ฐฌ๊ฐ์ง๋ก **range** ์ฟผ๋ฆฌ์ ์ฌ์ฉ์ด ๊ฐ๋ฅํ๋ค. ๊ธฐ๋ณธ์ ์ผ๋ก Elasticsearch ์์ ๋ ์ง ๊ฐ์ `2016-01-01` ๋๋ `2016-01-01T10:15:30` ๊ณผ ๊ฐ์ด JSON ์์ ์ผ๋ฐ์ ์ผ๋ก ์ฌ์ฉ๋๋ **ISO8601** ํ์์ ์ฌ์ฉํ๋ค. 

date ํ๋์ ๋ ์ง๊ฐ 2017-03-29 ์ดํ์ธ ๋ํ๋จผํธ๋ค์ ๊ฒ์ํด๋ณด์.

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
    
- `format` ์ต์
    
    `||`์ ์ฌ์ฉํด์ ์ฌ๋ฌ ๊ฐ์ ์๋ ฅ์ด ๊ฐ๋ฅํ๋ค. 
    
    ๋ค์์ date ํ๋์ ๊ฐ์ด 2016-12-31๋ถํฐ 2018 ์ด์  ์ฌ์ด์ ์๋ ๊ฐ๋ค์ ๊ฒ์ํ๋ ์ฟผ๋ฆฌ๋ค.
    
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
        
- ๋ ์ง๋ฅผ ๊ฒ์ํ  ๋ `now` ์์ฝ์ด๋ฅผ ์ฌ์ฉํ  ์ ์๋ค. ( + y, M, d, h, m, s, w)
    
    ๋ค์์ date์ ๊ฐ์ด 2016-1-1์์ 6๊ฐ์ ํ์ธ ๋ ๋ถํฐ ์ค๋๋ณด๋ค 365์ผ ์ ์ธ ๋  ์ฌ์ด์ ๋ฐ์ดํฐ๋ฅผ ๊ฐ์ ธ์ค๋ ์ฟผ๋ฆฌ๋ค.
    
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
        
    


> #### ๐ก ์ฐธ๊ณ  ๋งํฌ


[5. ๊ฒ์๊ณผ ์ฟผ๋ฆฌ - Query DSL](https://esbook.kimjmin.net/05-search)
