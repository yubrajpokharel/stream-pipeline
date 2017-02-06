package com.api.endpoints

import java.util
import java.util.{Collections, Properties}

import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.json.JSONObject
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Configuration, Profile}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, post}
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{jsonPath, status}
import org.springframework.test.web.servlet.{MockMvc, ResultActions}

import scala.collection.JavaConverters._

/**
  * Created by prayagupd
  * on 1/29/17.
  */

@RunWith(classOf[SpringRunner])
@SpringBootTest
@AutoConfigureMockMvc
class PayloadIngestionEndpointsIntegrationSpecs extends FunSuite with SpringTestContextManagement with Matchers {

  @Autowired val mockMvc: MockMvc = null

  implicit val streamingConfig = EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2181) //EventStreamConfig

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()
  } //FIXME make it EventStream.start()

  override protected def afterAll(): Unit = {
    super.afterAll()
    EmbeddedKafka.stop()
  }

  //http://stackoverflow.com/questions/29490113/kafka-get-broker-host-from-zookeeper
  test("health responseCode is API-001, when Eventstream is Up") {
    val response: ResultActions = mockMvc.perform(get("/health")).andDo(print())

    response.andExpect(status().isOk)
      .andExpect(jsonPath("$.responseCode").value("API-001"))
  }

  test("valildates the incoming payload with the defined schema, and responds with bad request(API-004) on failure") {
    val json =
      """
                {
                  "eventType" : "TestIngestionEvent",
                  "someField2"  : "someValue"
                }
      """.stripMargin

    val response: ResultActions = mockMvc.perform(post("/ingest").content(json)).andDo(print())

    response.andExpect(status().is(200))
      .andExpect(jsonPath("$.responseCode").value("API-004"))
  }

  test("accepts the JSON payload and publishes to eventstream and responds with success message") {
    val json =
      """
        {
          "eventType" : "TestIngestionEvent",
          "someField1"  : "someValue"
        }
      """.stripMargin.replaceAll("\\+s", "")

    val response: ResultActions = mockMvc.perform(post("/ingest").content(json)).andDo(print())

    //then
    val nativeKafkaConsumer = new KafkaConsumer[String, String](new Properties() {
      {
        put("bootstrap.servers", "localhost:9092") //streaming.config
        put("group.id", "consumer_group_test")
        put("auto.offset.reset", "earliest")
        put("key.deserializer", classOf[StringDeserializer].getName)
        put("value.deserializer", classOf[StringDeserializer].getName)
      }
    })

    assert(nativeKafkaConsumer.listTopics().asScala.map(_._1) == List("EventStream"))

    nativeKafkaConsumer.subscribe(util.Arrays.asList("EventStream"))

    assert(AdminUtils.topicExists(new ZkUtils(new ZkClient("localhost:2181", 10000, 15000),
      new ZkConnection("localhost:2181"), false), "EventStream"))

    val events: ConsumerRecords[String, String] = nativeKafkaConsumer.poll(1000)

    val persistedEvent = new JSONObject(events.asScala.map(_.value()).head)
    //    assert(persistedEvent.getString("eventType") == "SomeEventForIngestion")
    assert(persistedEvent.getString("someField1") == "someValue")
    //    assert(persistedEvent.getLong("createdTime") != 0l) //FIXME

    assert(events.partitions().size() == 1)
    assert(events.count() == 1)

    response.andExpect(status().isOk)
      .andExpect(jsonPath("$.responseCode").value("API-002"))
  }

  test("is able to ingest concurrent requests to the eventstream") {

    val nativeKafkaConsumer = new KafkaConsumer[String, String](new Properties() {
      {
        put("bootstrap.servers", "localhost:9092") //streaming.config
        put("group.id", "consumer_group_test_new")
        put("client.id", "TestEventConsumer")
        put("auto.offset.reset", "latest")
        put("key.deserializer", classOf[StringDeserializer].getName)
        put("value.deserializer", classOf[StringDeserializer].getName)
      }
    })

    nativeKafkaConsumer.subscribe(Collections.singletonList("EventStream"))

    assert(nativeKafkaConsumer.poll(1000).count() == 0)

    val requests = Range(0, 100).map(identifier => {
      s"""
        {
          "eventType" : "TestIngestionEvent",
          "someField1"  : "someValue-${identifier}"
        }
      """.stripMargin.replaceAll("\\+s", "")
    }).toList

    requests.map(json => {
      mockMvc.perform(post("/ingest").content(json)).andDo(print())
    }).foreach(x => x.andExpect(status().isOk))

    //then
    val events = nativeKafkaConsumer.poll(1000)

    assert(events.count() == 100)
  }
}

@Configuration
@Profile(Array("test"))
class TestConfiguration {

}
