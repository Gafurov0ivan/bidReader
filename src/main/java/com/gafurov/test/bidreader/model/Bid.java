package com.gafurov.test.bidreader.model;

import java.math.BigDecimal;

public class Bid {
    private final BigDecimal id;
    private final long timestamp;
    private final String type;
    private final String payload;

    public Bid(BigDecimal id, long timestamp, String type, String payload) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
    }

    public BigDecimal getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }
}
