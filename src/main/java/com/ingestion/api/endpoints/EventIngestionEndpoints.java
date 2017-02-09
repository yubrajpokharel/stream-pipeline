package com.ingestion.api.endpoints;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.ingestion.api.domain.AckNotification;
import com.ingestion.api.domain.HealthStatus;
import com.ingestion.api.validation.JsonSchemaValidator;
import eventstream.events.BaseEvent;
import eventstream.events.JsonEvent;
import eventstream.producer.fails.EventStreamProducerException;
import eventstream.producer.generic.GenericEventProducer;
import eventstream.state.EventStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

@RestController
public class EventIngestionEndpoints {

    private Logger logger = LogManager.getLogger(EventIngestionEndpoints.class);

    @Autowired
    GenericEventProducer eventProducer;

    @Autowired
    JsonSchemaValidator jsonSchemaValidator;

    @Autowired
    @Qualifier("schemaEventTypeLamda")
    Function<String, String> schemaEventTypeLamda;

    @Autowired
    @Qualifier("eventIdLambda")
    Function<String, String> eventIdLambda;

    @Autowired
    EventStream eventStream;

    @RequestMapping("/health")
    public HealthStatus health() {
        try {
            if (eventStream.activeNodes().size() > 0) {
                return new HealthStatus(UUID.randomUUID().toString(), "Green", eventStream.activeNodes().toString());
            }
            return new HealthStatus(UUID.randomUUID().toString(), "Red", "Eventstream is down");
        } catch (Exception e) {
            return new HealthStatus(UUID.randomUUID().toString(), "Red", e.getMessage());
        }
    }

    @RequestMapping(value = "/ingest", method = RequestMethod.POST)
    public AckNotification ingest(@RequestBody String payload) {

        //TODO create a LogService
        logger.info("payload={}", payload);

        try {
            if (jsonSchemaValidator.isValidPayload(payload, schemaEventTypeLamda)) {
                BaseEvent event = new JsonEvent(payload);
                BaseEvent publishedEvent = eventProducer.publish(event);
                logger.debug(publishedEvent.toJSON(publishedEvent));
                return new AckNotification(new AckNotification.AckPayload(eventIdLambda.apply(payload), "API-002",
                        "Payload accepted"), HttpStatus.OK);
            } else {
                logger.error("invalid request {}", eventIdLambda.apply(payload));
                return new AckNotification(new AckNotification.AckPayload(eventIdLambda.apply(payload), "API-004",
                        "Validation failed"), HttpStatus.BAD_REQUEST);
            }
        } catch (ProcessingException | EventStreamProducerException e) {
            logger.error("Could not persist the event, {}", e);
            return new AckNotification(new AckNotification.AckPayload(eventIdLambda.apply(payload), "API-005",
                    "API Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
