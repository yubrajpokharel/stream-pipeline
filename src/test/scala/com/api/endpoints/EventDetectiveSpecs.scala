package com.api.endpoints

import eventstream.events.BaseEvent
import org.scalatest.FunSuite

/**
  * Created by prayagupd
  * on 2/1/17.
  */
class EventDetectiveSpecs extends FunSuite {

  val toEvent = new EventDetective()

  test("returns the eventType") {
    toEvent.EVENTS_PACKAGE = "com.api.endpoints"
    val eventType = toEvent.eventType("TestEvento")
    assert(eventType.isPresent)
    assert(eventType.get() == classOf[TestEvento])
  }
}

class TestEvento extends BaseEvent {

}