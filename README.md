
eventstream pipeline
=========================


```
|                   |                          |
|                   |                          |------------------------
|    /endpoint      |      Eventstream driver  |   Eventstream         |
|                   |                          |  |  |  |  |  |  |  |  |
|                   |                          |------------------------
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

{"eventId":"aef35bbc-17db-4e4d-bcbe-e6fbffc4150c","responseCode":"API-002","responseMessage":"Payload accepted"}

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

add `createdTime` to event attribute. 

That way its kind of endpoint for ingestion, with validation layer

Define the eventType in the payload as below, based on your requirement.

```java
    @Bean
    Function<String, String> schemaEventType() {
        return payload -> new JSONObject(payload).getString("eventType");
    }
```
