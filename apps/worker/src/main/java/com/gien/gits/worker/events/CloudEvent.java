package com.gien.gits.worker.events;

import java.util.Map;
import java.util.Objects;

/**
 * In-process representation of a CloudEvent payload as defined by
 * specs/events/domain-events.asyncapi.json (components/schemas/CloudEvent).
 *
 * <p>This is an engineering mechanism record only — it mirrors the contract
 * field names exactly (specversion, id, source, type, time, subject,
 * datacontenttype, data) and does not invent additional fields. It is used to
 * carry domain events through Spring's in-process event bus; no real broker is
 * involved.
 */
public final class CloudEvent {

    private final String specversion;
    private final String id;
    private final String source;
    private final String type;
    private final String time;
    private final String subject;
    private final String datacontenttype;
    private final Map<String, Object> data;

    private CloudEvent(String specversion, String id, String source, String type, String time,
                       String subject, String datacontenttype, Map<String, Object> data) {
        this.specversion = specversion;
        this.id = id;
        this.source = source;
        this.type = type;
        this.time = time;
        this.subject = subject;
        this.datacontenttype = datacontenttype;
        this.data = data;
    }

    public String specversion() { return specversion; }
    public String id() { return id; }
    public String source() { return source; }
    public String type() { return type; }
    public String time() { return time; }
    public String subject() { return subject; }
    public String datacontenttype() { return datacontenttype; }
    public Map<String, Object> data() { return data; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String specversion = "1.0";
        private String id;
        private String source;
        private String type;
        private String time;
        private String subject;
        private String datacontenttype = "application/json";
        private Map<String, Object> data;

        public Builder specversion(String specversion) { this.specversion = specversion; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder time(String time) { this.time = time; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder datacontenttype(String datacontenttype) { this.datacontenttype = datacontenttype; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }

        public CloudEvent build() {
            Objects.requireNonNull(specversion, "specversion");
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(time, "time");
            Objects.requireNonNull(subject, "subject");
            Objects.requireNonNull(datacontenttype, "datacontenttype");
            Objects.requireNonNull(data, "data");
            return new CloudEvent(specversion, id, source, type, time, subject, datacontenttype, data);
        }
    }
}
