package com.api.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by prayagupd
 * on 1/30/17.
 */

public class AckNotification {
    @Getter @Setter private String eventId;
    @Getter @Setter private String responseCode;
    @Getter @Setter private String responseMessage;

    public AckNotification(String eventId, String responseCode, String desc) {
        this.eventId = eventId;
        this.responseCode = responseCode;
        this.responseMessage = desc;
    }
}
