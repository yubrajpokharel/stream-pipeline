package com.ingestion.api.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by prayagupd
 * on 1/29/17.
 */

public class HealthStatus {

    @Getter @Setter private String eventId;
    @Getter @Setter private String status;
    @Getter @Setter private String description;

    public HealthStatus(String eventId, String status, String description) {
        this.eventId = eventId;
        this.status = status;
        this.description = description;
    }
}
