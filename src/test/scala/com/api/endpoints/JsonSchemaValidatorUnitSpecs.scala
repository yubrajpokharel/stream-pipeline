package com.api.endpoints

import org.scalatest.{FunSpec, FunSuite}

/**
  * Created by prayagupd
  * on 2/3/17.
  */

class JsonSchemaValidatorUnitSpecs extends FunSpec {

  val schemaValidator = new JsonSchemaValidator

  describe("validates the JSON payload as success") {
    it("when there's no schema available") {
      val json =
        """
        {
           "eventType" : "WhateverEvent",
           "someField1" : "SomeValue1",
           "someField2" : "SomeValue2"
        }
        """.stripMargin

      assert(schemaValidator.isValidPayload(json))

    }

    it("when the payload matches against schema") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithSchema",
           "requiredField1" : "SomeValue1"
        }
        """.stripMargin

      assert(schemaValidator.isValidPayload(json))

    }

    it("even when the payload have extra not_required fields") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithSchema",
           "requiredField1" : "SomeValue1",
           "notRequiredField1" : "SomeValue2"
        }
        """.stripMargin

      assert(schemaValidator.isValidPayload(json))

    }

    it("when the nested object is present with content") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithNestedProperties",
           "requiredProperty1" : {
                "requiredProperty2" : "SomeValue"
           }
        }
        """.stripMargin

      assert(schemaValidator.isValidPayload(json))

    }

    it("even when the payload has undefined fields(because JSON as schemaless :) )") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithNoPropertiesDefined",
           "requiredField1" : "SomeValue1",
           "newField" : "SomeValue"
        }
        """.stripMargin

      assert(schemaValidator.isValidPayload(json))

    }
  }

  describe("validates the json as failure") {
    it("when the payload has no required fields") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithSchema",
           "notRequiredField1" : "SomeValue2"
        }
        """.stripMargin

      assert(!schemaValidator.isValidPayload(json))

    }

    it("when the payload has different value types than defined schema") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithSchema",
          "requiredField1" : 1
        }
        """.stripMargin

      assert(!schemaValidator.isValidPayload(json))

    }

    it("when the nested_property is present without required content") {
      val json =
        """
        {
           "eventType" : "IngestionEventWithNestedProperties",
           "requiredProperty1" : { }
        }
        """.stripMargin

      assert(!schemaValidator.isValidPayload(json))

    }
  }
}
