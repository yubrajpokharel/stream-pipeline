package com.ingestion.api.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

public class HealthStatus {

    @Getter @Setter private String eventId;
    @Getter @Setter private String responseCode;
    @Getter @Setter private String responseMessage;

    public HealthStatus(String eventId, String responseCode, String responseMessage) {
        this.eventId = eventId;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }
}
