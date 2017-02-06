package com.api.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by prayagupd
 * on 1/30/17.
 */

public class AckNotification extends ResponseEntity {

    public AckNotification(AckPayload ackPayload, HttpStatus httpStatus) {
        super(ackPayload, httpStatus);
    }

    public static class AckPayload {
        @Getter @Setter private String eventId;
        @Getter @Setter private String responseCode;
        @Getter @Setter private String responseMessage;

        public AckPayload(String eventId, String responseCode, String desc) {
            this.eventId = eventId;
            this.responseCode = responseCode;
            this.responseMessage = desc;
        }
    }
}
