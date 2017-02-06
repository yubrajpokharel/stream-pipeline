package com.api;

import eventstream.producer.generic.GenericEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

@SpringBootApplication
public class RESTApplication {

    @Value("${eventstream.name}")
    String eventStreamName;

    @Bean
    GenericEventProducer eventProducer() {
        return new GenericEventProducer(eventStreamName);
    }

    public static void main(String[] args) {
        SpringApplication.run(RESTApplication.class, args);
    }
}
