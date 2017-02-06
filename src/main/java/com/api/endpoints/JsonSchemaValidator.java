package com.api.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by prayagupd
 * on 2/3/17.
 */

@Component
public class JsonSchemaValidator {

    private Logger logger = LogManager.getLogger(JsonSchemaValidator.class);

    public boolean isValidPayload(String payload, Function<String, String> schemaSource) throws ProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        try {
            final JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromResource("/schema/" +
                     schemaSource.apply(payload) + ".json"));
            ProcessingReport validation = schema.validate(objectMapper.readTree(payload));
            validation.forEach(processingMessage ->
                    logger.error(processingMessage.getMessage())
            );
            return validation.isSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
            return true;
        }
    }
}
