package com.seristic.morphlib.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Small helper carrying contextual metadata for logs.
 * Immutable after build — use builder to construct.
 */
public final class LogContext {
    private final String correlationId;
    private final Map<String, String> data;

    private LogContext(String correlationId, Map<String, String> data) {
        this.correlationId = correlationId;
        this.data = Collections.unmodifiableMap(data);
    }

    public String correlationId() {
        return correlationId;
    }

    public Map<String, String> data() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, String> data = new HashMap<>();
        private String correlationId = UUID.randomUUID().toString();

        public Builder withCorrelationId(String id) {
            this.correlationId = id;
            return this;
        }

        public Builder add(String key, String value) {
            data.put(key, value);
            return this;
        }

        public LogContext build() {
            return new LogContext(correlationId, new HashMap<>(data));
        }
    }

    public static LogContext empty() {
        return new Builder().build();
    }
}
