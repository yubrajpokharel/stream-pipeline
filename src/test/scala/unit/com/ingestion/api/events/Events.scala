package com.ingestion.api.events

import com.eventstream.events.BaseEvent

/**
  * Created by prayagupd
  * on 2/2/17.
  */

abstract class SomeEventForIngestion(someField_1: String) extends BaseEvent {
  var someField1 = someField_1

  def this(){
    this("")
  }

  def getSomeField1 = someField1
  def setSomeField1(someField1: String) = this.someField1 = someField1
}
