
eventstream pipeline
=========================

![](EventPipeline.png)

tests
-----

```
mvn test
```

run-app
-------

```
mvn clean package

```

or 

```
$ mvn spring-boot:run
```

```
curl -XGET http://localhost:9000/ingestion-api/health

{ 
  "eventId":"09f41883-71bb-47a7-a27d-5fd0609a9d7d",
  "responseCode":"API-001",
  "responseMessage":"Green"
}

```

Ingestion
---------

```bash
curl -H "Content-Type: application/json" -X POST -d '{"MessageHeader" : { "EventId" : "some-uniq-id", "EventName" : "TestIngestionEvent"}, "someField1" : "someValue1"}' localhost:9000/ingestion-api/ingest

{"eventId":"some-uniq-id","responseCode":"API-002","responseMessage":"Payload accepted"}

```

JSON schema validation 
------------------------

Put the schema to validate in `resources/schema/EventName.json`

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "MessageHeader": {
      "type": "object",
      "properties": {
        "EventName": {
          "type": "string"
        }
      },
      "required": [
        "EventName"
      ]
    },
    "someField1": {
      "type": "string"
    }
  },
  "required": [
    "MessageHeader",
    "someField1"
  ]
}
```


append to the Eventstream
-------------------------

With this pipeline(one application), everything goes into one eventstream, configured in 
`application.properties`

```bash
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic "EventStream" --from-beginning

{"createdTime":1486065173552,"eventType":"TestIngestionEvent","requiredField1":"someValue1"}

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
