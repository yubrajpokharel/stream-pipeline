package com.ingestion.api.endpoints

import java.util.function.Function

import com.amazonaws.services.kinesis.model.AmazonKinesisException
import com.eventstream.producer.{EventProducer, EventProducerFactory}
import com.specs.ComponentSpecs
import integration.com.ingestion.api.endpoints.SpecsConfig.taggedConfig
import org.json.JSONObject
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.eventstream.factory.EmbeddedEventStreamFactory
import org.scalatest.eventstream.{ConsumerConfig, StreamConfig}
import org.scalatest.springboot.SpringTestContextManager
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Profile, PropertySource}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, post}
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{jsonPath, status}
import org.springframework.test.web.servlet.{MockMvc, ResultActions}

/**
  * Created by prayagupd
  * on 1/29/17.
  */

@RunWith(classOf[SpringRunner])
//@ContextConfiguration(classes = Array(classOf[TestConfiguration]))
@SpringBootTest
@AutoConfigureMockMvc
class EventIngestionEndpointsIntegrationSpecs extends ComponentSpecs with SpringTestContextManager with Matchers {

  @Autowired val mockMvc: MockMvc = null

  val StreamName = "Pipeline_Stream"
  var partitionId = ""

  implicit val streamingConfig = StreamConfig(streamTcpPort = 9092, streamStateTcpPort = 2181,
    stream = StreamName,  numOfPartition = 1)

  val eventStream = new EmbeddedEventStreamFactory().create()

  val WAIT_TIME = 2000

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    partitionId = eventStream.startBroker._2.head
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    eventStream.destroyBroker
  }

  //http://stackoverflow.com/questions/29490113/kafka-get-broker-host-from-zookeeper
  ignore("health status is Green, when Eventstream is Up", taggedConfig) {

    val response: ResultActions = mockMvc.perform(get("/health")).andDo(print())

    response.andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("Green"))
  }

  scenario("accepts the JSON payload, matches against the schema and publishes to event-stream and " +
    "then responds with success message", taggedConfig) {
    val json =
      """
        {
          "MessageHeader" : {
              "EventId" : "some-very-unique-id-for-valid-payload",
              "EventName" : "TestIngestionEvent"
           },
          "someField1"  : "someValue"
        }
      """.stripMargin.replaceAll("\\+s", "")

    eventStream.assertStreamExists(streamingConfig)

    val response: ResultActions = mockMvc.perform(post("/ingest").content(json)).andDo(print())

    //then
    Thread.sleep(WAIT_TIME)
    val events = eventStream.consumeEvent(streamingConfig,
      ConsumerConfig("testevent_stream_good_consumer", partitionId, "TRIM_HORIZON"), StreamName)

    assert(events.size == 1)

    val persistedEvent = events.head
    //    assert(persistedEvent.getString("eventType") == "SomeEventForIngestion")
    assert(persistedEvent.getString("someField1") == "someValue")
    //    assert(persistedEvent.getLong("createdTime") != 0l) //FIXME

    response.andExpect(status().isOk)
      .andExpect(jsonPath("$.eventId").value("some-very-unique-id-for-valid-payload"))
      .andExpect(jsonPath("$.responseCode").value("SUCCESS"))
  }

  scenario("responds back 500 when could not write to the EventStream", taggedConfig) {
    try {
      eventStream.destroyBroker
    } catch {
      case e: AmazonKinesisException => println(e.getMessage)
    }

    val json =
      """
                {
                  "MessageHeader" :{
                     "EventName" : "TestIngestionEvent",
                     "EventId" : "some-really-unique-id-for-server-failure"
                  },
                  "someField1"  : "someValue"
                }
      """.stripMargin

    val response: ResultActions = mockMvc.perform(post("/ingest").content(json)).andDo(print())

    response.andExpect(status().is(500))
      .andExpect(jsonPath("$.eventId").value("some-really-unique-id-for-server-failure"))
      .andExpect(jsonPath("$.responseCode").value("SRV_FAIL"))

  }

  scenario("health responseStatus is Red, when Eventstream is down", taggedConfig) {
    try {
      eventStream.destroyBroker
    } catch {
      case e: AmazonKinesisException => println(e.getMessage)
    }

    val response: ResultActions = mockMvc.perform(get("/health")).andDo(print())

    response.andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("Red"))
      .andExpect(jsonPath("$.description").value("Eventstream is down"))
  }

  scenario("responds with bad request, http status 400, on payload validation failure", taggedConfig) {
    val json =
      """
                {
                  "MessageHeader" :{
                     "EventName" : "TestIngestionEvent",
                     "EventId" : "some-unique-id-for-invalid-request"
                  },
                  "someField2"  : "someValue"
                }
      """.stripMargin

    val response: ResultActions = mockMvc.perform(post("/ingest").content(json)).andDo(print())

    response.andExpect(status().is(400))
      .andExpect(jsonPath("$.eventId").value("some-unique-id-for-invalid-request"))
      .andExpect(jsonPath("$.responseCode").value("VLDN_FAIL"))
  }

  ignore("is able to ingest concurrent requests to the eventstream", taggedConfig) {

    val initialEvents = eventStream.consumeEvent(streamingConfig,
      ConsumerConfig("testevent_stream_lots_of_events_consumer", partitionId, "TRIM_HORIZON"), StreamName)

    assert(initialEvents.isEmpty)

    println("preparing payload")
    val requests = Range(0, 10).map(identifier => {
      s"""
        {
          "MessageHeader" :{
             "EventName" : "TestIngestionEvent",
             "EventId" : "event-id-${identifier}-concurrent"
          },
          "someField1"  : "someValue-${identifier}"
        }
      """.stripMargin.replaceAll("\\+s", "")
    }).toList

    println("/ingesting payload")

    requests.map(json => {
      mockMvc.perform(post("/ingest").content(json)).andDo(print())
    }).foreach(response => {
      println("===========================")
      response.andExpect(status().is(200))
    })

    //then
    val events = eventStream.consumeEvent(streamingConfig,
      ConsumerConfig("testevent_stream_lots_of_events_consumer_count", partitionId, "earliest"), StreamName)

    assert(events.size == 10)
  }
}

//FIXME add controller as bean
@Configuration
@PropertySource(Array("application.properties"))
@Profile(Array("test"))
class TestConfiguration {

  @Value("${eventstream.name}")
  val eventStreamName: String = null

  @Bean
  def eventProducer: EventProducer = new EventProducerFactory().create(eventStreamName)

  @Bean
  def schemaEventType: Function[String, String] = {
    new Function[String, String] {
      override def apply(t: String): String = {
        new JSONObject(t).getString("eventType")
      }
    }
  }
}
