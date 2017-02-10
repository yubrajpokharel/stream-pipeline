package com.ingestion.api.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.ingestion.api.validation.fails.EventValidationRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.Function;

@Component
public class JsonSchemaValidator {

    private Logger logger = LogManager.getLogger(JsonSchemaValidator.class);

    public Map<Boolean, List<String>> isValidPayload(String payload, Function<String, String> schemaSourceLamda) {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        try {
            final JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromResource("/schema/" +
                     schemaSourceLamda.apply(payload) + ".json"));
            ProcessingReport validation = schema.validate(objectMapper.readTree(payload));
            List<String> validationMessages = new ArrayList<>();
            validation.forEach(processingMessage -> validationMessages.add(processingMessage.getMessage()));

            return new HashMap<Boolean, List<String>>(){{
                put(validation.isSuccess(), validationMessages);
            }};
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
            return new HashMap<Boolean, List<String>>(){{
                put(true, Collections.singletonList(e.getMessage()));
            }};
        } catch (ProcessingException e) {
            e.printStackTrace();
            throw new EventValidationRuntimeException("Event Schema Validation failed", e);
        }
    }
}
