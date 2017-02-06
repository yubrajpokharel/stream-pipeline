
eventstream pipeline
=========================


```
|                   |                          |
|                   |                          |
|    endpoint       |      Eventstream driver  |   Eventstream
|                   |                          |
|                   |                          |
|                   |                          |

```


tests
-----

```
mvn test
```

run-app
-------

```
$ mvn spring-boot:run
```

```
curl -XGET http://localhost:9000/health

{
"id": 1,
"eventId": "some value",
"status": "I'm Running"
}

```

Ingestion
---------

```bash
curl -H "Content-Type: application/json" -X POST -d '{"eventType" : "TestIngestionEvent", "someField1" : "someValue1"}' localhost:9000/ingest

{"eventId":"3f95719a-8f6f-4d67-9ee6-36421895139c","responseCode":"API-002","responseMessage":""}

```

JSON schema validation 
------------------------

```

```


write to the Eventstream
------------------------

```bash
/usr/local/kafka_2.11-0.10.1.1/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic "EventStream" --from-beginning

{"createdTime":1486065173552,"eventType":"TestIngestionEvent","someField1":"someValue1"}

```

Note
----

you need an event object `TestIngestionEvent` which extends `BaseEvent` in 
`com.api.events` package

Maybe I should just not map to a class, instead just add `createTime`
event attribute. 

That way its kind of endpoint for ingestion, but I will have to add a
 validation layer.
