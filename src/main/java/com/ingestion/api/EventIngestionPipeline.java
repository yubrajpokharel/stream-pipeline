package com.ingestion.api;

import com.eventstream.producer.EventProducer;
import com.eventstream.producer.EventProducerFactory;
import com.eventstream.state.EventStream;
import com.eventstream.state.factory.EventStreamFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.function.Function;

/**
 * java singleton beans initialisation
 * Created by prayagupd
 * on 1/29/17.
 */

@SpringBootApplication
public class EventIngestionPipeline {

    @Value("${eventstream.name}")
    String eventStreamName;

    @Bean
    EventProducer eventProducer() {
        return new EventProducerFactory().create(eventStreamName);
    }

    @Bean
    @Qualifier("schemaEventTypeLambda")
    Function<String, String> schemaEventTypeLamda() {
        return payload -> new JSONObject(payload).getJSONObject("MessageHeader").getString("EventName");
    }

    @Bean
    @Qualifier("eventIdLambda")
    Function<String, String> eventIdLamda() {
        //FIXME when there's no eventId found, throw proper response
        //it would not respond anything otherwise
        return payload -> new JSONObject(payload).getJSONObject("MessageHeader").getString("EventId");
    }

    @Bean
    EventStream eventStream(){
        return new EventStreamFactory().create();
    }

    @Bean
    public ViewResolver getViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("templates/");
        return viewResolver;
    }

    public static void main(String[] args) {
        SpringApplication.run(EventIngestionPipeline.class, args);
    }
}
