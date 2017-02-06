package com.api.endpoints;

import com.api.domain.AckNotification;
import com.api.domain.AckNotification.AckPayload;
import com.api.domain.HealthStatus;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import eventstream.events.BaseEvent;
import eventstream.events.JsonEvent;
import eventstream.producer.generic.GenericEventProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

@RestController
public class PayloadIngestionEndpoints {

    private Logger logger = LogManager.getLogger(PayloadIngestionEndpoints.class);

    @Autowired
    GenericEventProducer eventProducer;

    @Autowired
    JsonSchemaValidator jsonSchemaValidator;

    @Autowired
    Function<String, String> schemaEventType;

    @RequestMapping("/health")
    public HealthStatus health() {
        return new HealthStatus(UUID.randomUUID().toString(), "API-001", "Green");
    }

    @RequestMapping(value = "/ingest", method = RequestMethod.POST)
    public AckNotification ingest(@RequestBody String payload) {

        //TODO create a LogService
        logger.info("payload={}", payload);

        try {
            if (jsonSchemaValidator.isValidPayload(payload, schemaEventType)) {
                BaseEvent event = new JsonEvent(payload);
                BaseEvent publishedEvent = eventProducer.publish(event);
                logger.debug(publishedEvent.toJSON(publishedEvent));
                return new AckNotification(new AckPayload(UUID.randomUUID().toString(), "API-002",
                        "Payload accepted"), HttpStatus.OK);
            } else {
                return new AckNotification(new AckPayload(UUID.randomUUID().toString(), "API-004",
                        "Validation failed"), HttpStatus.BAD_REQUEST);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (ProcessingException e) {
            e.printStackTrace();
        }
        return new AckNotification(new AckPayload(UUID.randomUUID().toString(), "API-005",
                "API Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
