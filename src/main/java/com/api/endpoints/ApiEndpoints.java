package com.api.endpoints;

import com.api.domain.AckNotification;
import com.api.domain.HealthStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import eventstream.events.BaseEvent;
import eventstream.events.JsonEvent;
import eventstream.producer.generic.GenericEventProducer;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    @Autowired
    EventDetective eventDetective;

    @RequestMapping("/health")
    public HealthStatus health() {
        return new HealthStatus(UUID.randomUUID().toString(), "API-001", "Green");
    }

    @RequestMapping(value = "/ingest", method = RequestMethod.POST)
    public AckNotification ingest(@RequestBody String payload) {

        //TODO create a LogService
        logger.info(payload);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            final JsonSchema schema = factory.getJsonSchema(JsonLoader.fromResource("/schema/" +
                    new JSONObject(payload).getString("eventType") + ".json"));
            val validation = schema.validate(objectMapper.readTree(payload));
            if (validation.isSuccess()) {
                BaseEvent event = new JsonEvent(payload);
                BaseEvent publishedEvent = eventProducer.publish(event);
                logger.debug(publishedEvent.toJSON(publishedEvent));
                return new AckNotification(UUID.randomUUID().toString(), "API-002", "");
            } else {
                return new AckNotification(UUID.randomUUID().toString(), "API-004", "Bad request.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (ProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AckNotification(UUID.randomUUID().toString(), "API-005", "Server failed.");
    }
}
