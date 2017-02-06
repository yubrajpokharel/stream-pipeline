package com.api;

import eventstream.producer.generic.GenericEventProducer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

@SpringBootApplication
public class EventIngestionPipeline {

    @Value("${eventstream.name}")
    String eventStreamName;

    @Bean
    GenericEventProducer eventProducer() {
        return new GenericEventProducer(eventStreamName);
    }

    @Bean
    Function<String, String> schemaEventType() {
        return payload -> new JSONObject(payload).getJSONObject("MessageHeader").getString("EventName");
    }

    public static void main(String[] args) {
        SpringApplication.run(EventIngestionPipeline.class, args);
    }
}
