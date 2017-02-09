package com.ingestion.api.validation.fails;

/**
 * Created by prayagupd
 * on 2/3/17.
 */

public class EventValidationRuntimeException extends RuntimeException {

    public EventValidationRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
