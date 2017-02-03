package com.api.endpoints;

import com.api.domain.AckNotification;
import com.api.domain.HealthStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventstream.events.BaseEvent;
import eventstream.producer.generic.GenericEventProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

@RestController
public class ApiEndpoints {

    private Logger logger = LogManager.getLogger(ApiEndpoints.class);

    //FIXME read the stream name from config??
    private GenericEventProducer eventProducer = new GenericEventProducer("EventStream");

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    EventFinder eventFinder;

    @RequestMapping("/health")
    public HealthStatus health() {
        return new HealthStatus(counter.incrementAndGet(), "some value", "I'm Running");
    }

    @RequestMapping(value = "/ingest", method = RequestMethod.POST)
    public AckNotification ingest(@RequestBody String payload) {

        //TODO create a LogService
        logger.info(payload);

        JSONObject jsonPayload = new JSONObject(payload);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            BaseEvent event = objectMapper.readValue(payload,
                    eventFinder.eventType(jsonPayload.getString("eventType")).get());
            event.setCreatedTime(new Date());
            BaseEvent publishedEvent = eventProducer.publish(event);
            System.out.println(publishedEvent.toJSON(publishedEvent));
            return new AckNotification(UUID.randomUUID().toString(), "Success");
        } catch (IOException e) {
            //FIXME exception handling
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new AckNotification(UUID.randomUUID().toString(), "Failed");
    }
}
