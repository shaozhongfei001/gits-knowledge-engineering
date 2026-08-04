package com.gien.gits.ontology.event;

import java.util.Map;

/**
 * CloudEvent v1.0 record — the canonical envelope for all domain events
 * produced by the GITS Knowledge Engineering system.
 *
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0/json-format.md">CloudEvents JSON Format</a>
 */
public record CloudEvent(
        String specversion,
        String id,
        String source,
        String type,
        String time,
        String subject,
        String datacontenttype,
        Map<String, Object> data
) {
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

        private Builder() {}

        public Builder specversion(String specversion) { this.specversion = specversion; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder time(String time) { this.time = time; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder datacontenttype(String datacontenttype) { this.datacontenttype = datacontenttype; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }

        public CloudEvent build() {
            return new CloudEvent(specversion, id, source, type, time, subject, datacontenttype, data);
        }
    }
}
